package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.RetryConstraint;
import io.jenetics.util.IntRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public sealed interface Solver {
  double fitness();

  Model model();

  static <M extends TetrapolarMeasurement> Step1<M> of(double base, Metrics.Length units, Model origin) {
    return new SolverBuilder<>(base, units, origin);
  }

  sealed interface Step1<M extends TetrapolarMeasurement> {
    Step2<M> system1x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Step2<M> system1x2(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  sealed interface Step2<M extends TetrapolarMeasurement> {
    Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Builder<Solver> system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  final class SolverBuilder<M extends TetrapolarMeasurement> implements Step1<M>, Step2<M>, Builder<Solver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolverBuilder.class);
    private static final int SIZE = 1 << 9;

    private record SolverRecord(double fitness, Model model) implements Solver {
      private SolverRecord {
        Objects.requireNonNull(model);
      }

      @Override
      public String toString() {
        return "SolverRecord{fitness = %.4f, model = %s}".formatted(fitness, model);
      }
    }

    private final double base;
    private final Metrics.Length units;
    private final InvertibleCodec<Model, IntegerGene> modelFactory;
    private final Collection<ParametricFunctional> parametricFunctionals = new ArrayList<>();
    private final Cache<Double, Solver> alphaCache = Caffeine.newBuilder().maximumSize(SIZE).build();
    /**
     * Элита измеряется долей от популяции.
     * Эмпирическое правило для сложных многопараметрических задач: элита должна составлять от 0.5% до 1% от размера популяции.
     * При N = 1024 -> 1024 * 0.006 = 6 особей.
     * При N = 4096 -> 4096 * 0.006 = 25 особей.
     */
    private final int eliteSize = Math.max(3, (int) Math.floor(SIZE * 0.006));
    /**
     * Жесткий турнирный селектор для родителей.
     * В теории рост размера турнира должен быть логарифмическим относительно роста популяции.
     * Полноразмерный перебор по степеням двойки дает базовую скорость отбора.
     * Двоичный логарифм отражает «глубину» деления популяции.
     * Вычитание константы 5 удерживает турнир в рамках безопасного диапазона интенсивности (от 2 до 10):
     * При N = 1024 : 5 особей.
     * При N = 4096 : 7 особей.
     * При N = 16384 : 9 особей (для сверхбольших популяций).
     */
    private final int parentTournament = Math.max(2, (int) (StrictMath.log(SIZE) / StrictMath.log(2.0)) - 5);
    /**
     * Мягкий турнирный селектор для оставшейся части выживающих особей.
     * Селектор потомков всегда должен быть мягче, чем родительский, чтобы сохранять новые мутации.
     * На практике его размер берут как половину или около того от турнира родителей, но не ниже двух.
     * Формула обеспечивает плавный шаг отставания от родительского турнира:
     * При k = 5 : 4 особи.
     * При k = 7 : 5 особей.
     */
    private final int offspringTournament = Math.max(2, (int) Math.floor(parentTournament * 0.6) + 1);

    public SolverBuilder(double base, Metrics.Length units, Model origin) {
      if (base > 0) {
        this.base = base;
      }
      else {
        throw new IllegalArgumentException("base = %f must be positive".formatted(base));
      }
      this.units = Objects.requireNonNull(units);

      record KScaler(int scale) {
        KScaler {
          scale = Math.max(1, Math.abs(scale));
        }

        double toK(int x) {
          return Math.clamp(Math.signum(x) * StrictMath.log1p(Math.abs(x)) / StrictMath.log1p(scale), -1.0, 1.0);
        }

        int fromK(double k) {
          return Math.clamp(Numbers.toInt(Math.signum(k) * (StrictMath.pow(scale + 1.0, Math.abs(k)) - 1)), -scale, scale);
        }

        static IntRange range(int index) {
          return new IntRange(Math.min(index, 0), Math.max(0, index));
        }
      }

      modelFactory = switch (origin) {
        case Model.Layer2Relative(K k, double h) -> {
          KScaler kScaler = new KScaler(1000);
          int kIndex = kScaler.fromK(k.value());
          IntRange hIndexRange = new IntRange(0, Numbers.toInt(Metrics.Length.METRE.to(h, MetricPrefix.MICRO(Units.METRE))));
          yield InvertibleCodec.of(
              () -> Genotype.of(
                  IntegerChromosome.of(KScaler.range(kIndex)),
                  IntegerChromosome.of(hIndexRange)
              ),
              gt -> new Model.Layer2Relative(
                  K.of(kScaler.toK(gt.get(0).as(IntegerChromosome.class).gene().allele())),
                  Metrics.Length.MICRO.toSI(gt.get(1).as(IntegerChromosome.class).gene().allele())
              ),
              input -> switch (input) {
                case Model.Layer2Relative(K k1, double h1) -> Genotype.of(
                    IntegerChromosome.of(KScaler.range(kIndex), kScaler.fromK(k1.value())),
                    IntegerChromosome.of(hIndexRange, Numbers.toInt(Metrics.Length.METRE.to(h1, MetricPrefix.MICRO(Units.METRE))))
                );
                default -> throw new IllegalStateException("Unexpected value: " + input);
              });
        }
        default -> throw new IllegalStateException("Unexpected value: " + origin);
      };
    }

    @Override
    public Step2<M> system1x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 3, builderFunction);
      return this;
    }

    @Override
    public Step2<M> system1x2(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 2, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(5, 3, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 4, builderFunction);
      return this;
    }

    private void addSystem(int factorFirst, int factorLast, Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      parametricFunctionals.add(
          ParametricFunctional.builder(units)
              .system(s -> s.tetrapolar(base * factorFirst, base * factorLast).absError(0.1))
              .measurements(_ -> builderFunction.apply(TetrapolarMeasurement.builder()))
              .build()
      );
    }

    private Solver find(double alpha) {
      return alphaCache.get(alpha, this::innerFind);
    }

    private Solver innerFind(double alpha) {
      Cache<Model, Double> fitnessCache = Caffeine.newBuilder().maximumSize(SIZE).build();
      LongAdder realEvaluationsCounter = new LongAdder();
      LongAdder totalEvaluationsCounter = new LongAdder();
      ToDoubleFunction<Model> fitness = model -> {
        totalEvaluationsCounter.increment();
        return fitnessCache.get(model, m -> {
          realEvaluationsCounter.increment();
          return DoubleStream.concat(
                  parametricFunctionals.stream().mapToDouble(f -> alpha * f.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG).applyAsDouble(m)),
                  parametricFunctionals.stream().mapToDouble(f -> f.misfit().applyAsDouble(m)).map(x -> x * x)
              )
              .takeWhile(Double::isFinite).boxed().collect(
                  Collectors.teeing(
                      Collectors.reducing(Double::sum),
                      Collectors.counting(),
                      (sum, count) -> count == parametricFunctionals.size() * 2L ?
                          StrictMath.sqrt(sum.orElseThrow()) : Double.POSITIVE_INFINITY
                  )
              );
        });
      };

      EvolutionResult<IntegerGene, Double> evolutionState = null;

      try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int epoch = 0, totalEpochs = 1 << 4; epoch < totalEpochs; epoch++) {
          double mutationRate = Math.clamp(1.0 / Math.E / Integer.highestOneBit(epoch), 0.01, 1.0 / Math.E);
          Engine<IntegerGene, Double> engine = Engine.builder(fitness::applyAsDouble, modelFactory)
              .populationSize(SIZE)
              .optimize(Optimize.MINIMUM)
              .executor(executor)
              .survivorsSelector(new EliteSelector<>(eliteSize))
              .selector(new TournamentSelector<>(parentTournament))
              .offspringSelector(new TournamentSelector<>(offspringTournament))
              .alterers(new Mutator<>(mutationRate), new LineCrossover<>(0.15), new MultiPointCrossover<>(0.6, 2))
              .constraint(RetryConstraint.of(modelFactory, m -> Double.isFinite(fitness.applyAsDouble(m))))
              .build();

          var stream = (evolutionState == null) ? engine.stream() : engine.stream(evolutionState);
          evolutionState = stream.limit(1 << 4).collect(EvolutionResult.toBestEvolutionResult());

          double bestFitness = evolutionState.bestFitness();
          Model currentBestInput = modelFactory.decode(evolutionState.bestPhenotype().genotype());

          LOGGER.atDebug().addKeyValue("Эпоха", "%02d/%02d".formatted(epoch + 1, totalEpochs))
              .addKeyValue("Поколение", "%03d".formatted(evolutionState.generation()))
              .addKeyValue("Мутация", "%04.1f".formatted(mutationRate * 100))
              .addKeyValue("Невязка", "%.4f".formatted(bestFitness))
              .log(currentBestInput::toString);

          if (bestFitness < 1.0E-6) {
            LOGGER.atDebug().log(() -> "--> Минимум найден досрочно!");
            break;
          }
        }
        SolverRecord solverRecord = new SolverRecord(evolutionState.bestFitness(), modelFactory.decode(evolutionState.bestPhenotype().genotype()));
        LOGGER.atDebug()
            .addKeyValue("Вычислений", realEvaluationsCounter::sum)
            .addKeyValue("Всего попыток", totalEvaluationsCounter::sum)
            .addKeyValue("Экономия за счет кэша", () -> "%.0f %%".formatted((1.0 - realEvaluationsCounter.doubleValue() / totalEvaluationsCounter.sum()) * 100))
            .log(solverRecord::toString);
        return solverRecord;
      }
    }

    @Override
    public Solver build() {
      LOGGER.atDebug().addKeyValue("Популяция", SIZE).addKeyValue("Элита", eliteSize)
          .addKeyValue("Турнир родителей", parentTournament).addKeyValue("Турнир детей", offspringTournament)
          .log("");
      return DoubleStream.of(1.0, 0.1, 0.01, 0.001, 0.000_1).mapToObj(alpha -> {
            Solver solver = find(alpha);
            LOGGER.atInfo()
                .addKeyValue("alpha", () -> "%.4f".formatted(alpha))
                .log(solver::toString);
            return solver;
          }).min(Comparator.comparingDouble(Solver::fitness))
          .orElseThrow();
    }
  }
}
