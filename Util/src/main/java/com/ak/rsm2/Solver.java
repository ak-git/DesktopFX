package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.InvertibleCodec;
import io.jenetics.engine.RetryConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
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
    private static final RandomGenerator RANDOM = new SecureRandom();
    private static final int SIZE = 1 << 8;

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
    private final InvertibleCodec<Model, AnyGene<Number>> modelFactory;
    private final Collection<ParametricFunctional> parametricFunctionals = new ArrayList<>();
    private final Cache<Double, Solver> alphaCache = Caffeine.newBuilder().maximumSize(SIZE).build();

    public SolverBuilder(double base, Metrics.Length units, Model origin) {
      if (base > 0) {
        this.base = base;
      }
      else {
        throw new IllegalArgumentException("base = %f must be positive".formatted(base));
      }
      this.units = Objects.requireNonNull(units);
      modelFactory = InvertibleCodec.of(
          () -> switch (origin) {
            case Model.Layer2Relative(K k, double h) -> Genotype.of(
                AnyChromosome.of(() -> RANDOM.nextDouble(
                    Math.min(k.value(), 0.0), Math.max(0.0, k.value()))
                ),
                AnyChromosome.of(() -> RANDOM.nextDouble(0, h))
            );
            case Model.Layer2RelativeDh(Model.Layer2Relative layer2Relative, double dh) -> Genotype.of(
                AnyChromosome.of(() -> RANDOM.nextDouble(
                    Math.min(layer2Relative.k().value(), 0.0), Math.max(0.0, layer2Relative.k().value()))
                ),
                AnyChromosome.of(() -> RANDOM.nextDouble(0, layer2Relative.h())),
                AnyChromosome.of(() -> RANDOM.nextDouble(Math.min(dh, 0.0), Math.max(0.0, dh)))
            );
            case Model.Layer3Absolute layer3Absolute -> throw new IllegalArgumentException(layer3Absolute.toString());
            case Model.Layer3AbsoluteDRho2(Model.Layer3Absolute layer3Absolute, Model.P dp, double dRho2) ->
                Genotype.of(
                    AnyChromosome.of(() -> RANDOM.nextDouble(1.5, layer3Absolute.rho1())),
                    AnyChromosome.of(() -> RANDOM.nextDouble(1.5, layer3Absolute.rho2())),
                    AnyChromosome.of(() -> RANDOM.nextDouble(1.5, layer3Absolute.rho3())),
                    AnyChromosome.of(() -> RANDOM.nextInt(layer3Absolute.p().p1())),
                    AnyChromosome.of(() -> RANDOM.nextInt(layer3Absolute.p().p2mp1())),
                    AnyChromosome.of(() -> RANDOM.nextInt(dp.p1())),
                    AnyChromosome.of(() -> RANDOM.nextInt(dp.p2mp1())),
                    AnyChromosome.of(() -> RANDOM.nextDouble(dRho2))
                );
          },
          gt -> switch (origin) {
            case Model.Layer2Relative _ -> new Model.Layer2Relative(
                K.of(gt.get(0).gene().allele().doubleValue()),
                gt.get(1).gene().allele().doubleValue()
            );
            case Model.Layer2RelativeDh _ -> new Model.Layer2RelativeDh(
                K.of(gt.get(0).gene().allele().doubleValue()),
                gt.get(1).gene().allele().doubleValue(),
                gt.get(2).gene().allele().doubleValue()
            );
            case Model.Layer3Absolute layer3Absolute -> throw new IllegalArgumentException(layer3Absolute.toString());
            case Model.Layer3AbsoluteDRho2 layer3AbsoluteDRho2 -> new Model.Layer3AbsoluteDRho2(
                new Model.Layer3Absolute(
                    gt.get(0).gene().allele().doubleValue(),
                    gt.get(1).gene().allele().doubleValue(),
                    gt.get(2).gene().allele().doubleValue(),
                    layer3AbsoluteDRho2.layer3Absolute().hStep(),
                    new Model.P(gt.get(3).gene().allele().intValue(), gt.get(4).gene().allele().intValue())),
                new Model.P(gt.get(5).gene().allele().intValue(), gt.get(6).gene().allele().intValue()),
                gt.get(7).gene().allele().doubleValue()
            );
          },
          input -> switch (input) {
            case Model.Layer2Relative(K k, double h) -> Genotype.of(
                AnyChromosome.of(k::value),
                AnyChromosome.of(() -> h)
            );
            case Model.Layer2RelativeDh(Model.Layer2Relative layer2Relative, double dh) -> Genotype.of(
                AnyChromosome.of(() -> layer2Relative.k().value()),
                AnyChromosome.of(layer2Relative::h),
                AnyChromosome.of(() -> dh)
            );
            case Model.Layer3Absolute layer3Absolute -> throw new IllegalArgumentException(layer3Absolute.toString());
            case Model.Layer3AbsoluteDRho2(Model.Layer3Absolute layer3Absolute, Model.P dp, double dRho2) ->
                Genotype.of(
                    AnyChromosome.of(layer3Absolute::rho1),
                    AnyChromosome.of(layer3Absolute::rho2),
                    AnyChromosome.of(layer3Absolute::rho3),
                    AnyChromosome.of(() -> layer3Absolute.p().p1()),
                    AnyChromosome.of(() -> layer3Absolute.p().p2mp1()),
                    AnyChromosome.of(dp::p1),
                    AnyChromosome.of(dp::p2mp1),
                    AnyChromosome.of(() -> dRho2)
                );
          }
      );
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

      EvolutionResult<AnyGene<Number>, Double> evolutionState = null;

      try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int epoch = 0, totalEpochs = 1 << 4; epoch < totalEpochs; epoch++) {
          double mutationRate = Math.clamp(1.0 / Math.E / Integer.highestOneBit(epoch), 0.01, 1.0 / Math.E);
          Engine<AnyGene<Number>, Double> engine = Engine.builder(fitness::applyAsDouble, modelFactory)
              .populationSize(SIZE)
              .optimize(Optimize.MINIMUM)
              .executor(executor)
              .selector(new TournamentSelector<>(5)) // жесткий турнирный селектор для родителей
              .survivorsSelector(new EliteSelector<>(5)) // элитный селектор для выживших
              .offspringSelector(new TournamentSelector<>(4)) // турнирный селектор для оставшейся части выживающих особей
              .alterers(new Mutator<>(mutationRate), new MultiPointCrossover<>(0.6, 2))
              .constraint(RetryConstraint.of(modelFactory, m -> Double.isFinite(fitness.applyAsDouble(m))))
              .build();

          var stream = (evolutionState == null) ? engine.stream() : engine.stream(evolutionState);
          evolutionState = stream.limit(1 << 4).collect(EvolutionResult.toBestEvolutionResult());

          double bestFitness = evolutionState.bestFitness();
          Model currentBestInput = modelFactory.decode(evolutionState.bestPhenotype().genotype());

          LOGGER.atDebug().log("Эпоха {}/{} завершена | Поколение: {} | Мутация: {} % | Невязка: {} | {}",
              "%02d".formatted(epoch + 1), "%02d".formatted(totalEpochs), "%03d".formatted(evolutionState.generation()),
              "%04.1f".formatted(mutationRate * 100), "%.4f".formatted(bestFitness), currentBestInput);

          if (bestFitness < 1.0E-6) {
            LOGGER.atDebug().log(() -> "--> Минимум найден досрочно!");
            break;
          }
        }
        SolverRecord solverRecord = new SolverRecord(evolutionState.bestFitness(), modelFactory.decode(evolutionState.bestPhenotype().genotype()));
        LOGGER.atDebug()
            .addKeyValue("Вычислений", realEvaluationsCounter::sum)
            .addKeyValue("Всего попыток", totalEvaluationsCounter::sum)
            .addKeyValue("Экономия за счет кэша", () -> "%.0f%%".formatted((1.0 - realEvaluationsCounter.doubleValue() / totalEvaluationsCounter.sum()) * 100))
            .log(solverRecord::toString);
        return solverRecord;
      }
    }

    @Override
    public Solver build() {
      return DoubleStream.of(10.0, 1.0, 0.1, 0.01).mapToObj(alpha -> {
            Solver solver = find(alpha);
            LOGGER.atInfo()
                .addKeyValue("alpha", () -> "%.4f".formatted(alpha))
                .log("{}", solver);
            return solver;
          }).min(Comparator.comparingDouble(Solver::fitness))
          .orElseThrow();
    }
  }
}
