package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import org.apache.commons.math4.legacy.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math4.legacy.analysis.interpolation.UnivariateInterpolator;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public sealed interface Solver {
  record Alpha(double value) {
    public Alpha {
      if (value < 0.0) {
        throw new IllegalArgumentException("value %f < 0".formatted(value));
      }
    }

    @Override
    public String toString() {
      return "alpha = %.4f".formatted(value);
    }
  }

  record Solution(Alpha alpha, double fitness, Model model) {
    public Solution {
      Objects.requireNonNull(alpha);
      if (fitness < 0.0) {
        throw new IllegalArgumentException("fitness %f < 0".formatted(fitness));
      }
      Objects.requireNonNull(model);
    }

    @Override
    public String toString() {
      return "Solution{%s; fitness = %.4f; %s}".formatted(alpha, fitness, model);
    }
  }

  Collection<Solution> solve(double... alphas);

  Solution solve();

  static <M extends TetrapolarMeasurement> Step1<M> of(double base, Metrics.Length units) {
    return new SolverBuilder<>(base, units);
  }

  sealed interface Step1<M extends TetrapolarMeasurement> {
    Step2<M> system1x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Step2<M> system1x2(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  sealed interface Step2<M extends TetrapolarMeasurement> {
    Step4<M> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Step3 system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  sealed interface Step3 {
    Builder<Solver> origin(Function<Stream<ElectrodeSystem.Inexact>, Model> modelFunction);
  }

  sealed interface Step4<M extends TetrapolarMeasurement> extends Step3 {
    Step5<M> system2x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  sealed interface Step5<M extends TetrapolarMeasurement> extends Step3 {
    Step3 system6x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  final class SolverBuilder<M extends TetrapolarMeasurement> implements Step1<M>, Step2<M>, Step4<M>, Step5<M>, Builder<Solver> {
    private static final class GeneticSolver implements Solver {
      private static final Logger LOGGER = LoggerFactory.getLogger(GeneticSolver.class);
      private final int size;
      /**
       * Элита измеряется долей от популяции.
       * Эмпирическое правило для сложных многопараметрических задач: элита должна составлять от 0.5% до 1% от размера популяции.
       * При N = 1024 -> 1024 * 0.006 = 6 особей.
       * При N = 4096 -> 4096 * 0.006 = 25 особей.
       */
      private final int eliteSize;
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
      private final int parentSize;
      /**
       * Мягкий турнирный селектор для оставшейся части выживающих особей.
       * Селектор потомков всегда должен быть мягче, чем родительский, чтобы сохранять новые мутации.
       * На практике его размер берут как половину или около того от турнира родителей, но не ниже двух.
       * Формула обеспечивает плавный шаг отставания от родительского турнира:
       * При k = 5 : 4 особи.
       * При k = 7 : 5 особей.
       */
      private final int offspringSize;
      private final Collection<ParametricFunctional> parametricFunctionals;
      private final InvertibleCodec<Model, AnyGene<Number>> modelFactory;
      private final Cache<Alpha, Solution> alphaCache = Caffeine.newBuilder().maximumSize(1 << 6).build();
      private @Nullable ISeq<Phenotype<AnyGene<Number>, Double>> resetPopulation;

      private GeneticSolver(Collection<ParametricFunctional> parametricFunctionals, InvertibleCodec<Model, AnyGene<Number>> modelFactory,
                            int size) {
        this.parametricFunctionals = Objects.requireNonNull(parametricFunctionals);
        this.modelFactory = Objects.requireNonNull(modelFactory);
        this.size = size;
        eliteSize = Math.max(3, (int) Math.floor(size * 0.006));
        parentSize = Math.max(2, (int) (StrictMath.log(size) / StrictMath.log(2.0)) - 5);
        offspringSize = Math.max(2, (int) Math.floor(parentSize * 0.6) + 1);
      }

      GeneticSolver(Collection<ParametricFunctional> parametricFunctionals, Model origin, int size) {
        this(parametricFunctionals, switch (origin) {
          case Model.Layer2Relative(K kExtremal, double hExtremal) -> InvertibleCodec.of(
              () -> Genotype.of(
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(Math.min(kExtremal.value(), 0), Math.max(0, kExtremal.value()))),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(0, hExtremal))
              ),
              chromosomes -> new Model.Layer2Relative(
                  K.of(chromosomes.get(0).gene().allele().doubleValue()),
                  chromosomes.get(1).gene().allele().doubleValue()
              ),
              model -> {
                if (model instanceof Model.Layer2Relative(K k, double h)) {
                  return Genotype.of(AnyChromosome.of(k::value), AnyChromosome.of(() -> h));
                }
                else {
                  throw new IllegalArgumentException("Unexpected value: " + model);
                }
              }
          );
          case Model.Layer2RelativeDh(Model.Layer2Model layer2Extremal, double dhExtremal) -> InvertibleCodec.of(
              () -> Genotype.of(
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(
                      Math.min(layer2Extremal.k().value(), 0), Math.max(0, layer2Extremal.k().value()))),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(0, layer2Extremal.h())),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(Math.min(dhExtremal, 0), Math.max(0, dhExtremal)))
              ),
              chromosomes -> new Model.Layer2RelativeDh(
                  K.of(chromosomes.get(0).gene().allele().doubleValue()),
                  chromosomes.get(1).gene().allele().doubleValue(),
                  chromosomes.get(2).gene().allele().doubleValue()
              ),
              model -> {
                if (model instanceof Model.Layer2RelativeDh(Model.Layer2Relative layer2Relative, double dh)) {
                  return Genotype.of(
                      AnyChromosome.of(() -> layer2Relative.k().value()),
                      AnyChromosome.of(layer2Relative::h),
                      AnyChromosome.of(() -> dh)
                  );
                }
                else {
                  throw new IllegalArgumentException("Unexpected value: " + model);
                }
              });
          case Model.Layer3Relative layer3Relative ->
              throw new IllegalArgumentException("Unexpected value: " + layer3Relative);
          case Model.Layer3RelativeDh(Model.Layer3Relative layer3Extremal, Model.P dpExtremal) -> InvertibleCodec.of(
              () -> Genotype.of(
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(
                      Math.min(layer3Extremal.k1().value(), 0), Math.max(0, layer3Extremal.k1().value()))),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(
                      Math.min(layer3Extremal.k2().value(), 0), Math.max(0, layer3Extremal.k2().value()))),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(1, layer3Extremal.p().p1() + 1)),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(1, layer3Extremal.p().p2mp1() + 1))
              ),
              chromosomes -> {
                Model.Layer3Relative layer3Relative = new Model.Layer3Relative(
                    K.of(chromosomes.get(0).gene().allele().doubleValue()),
                    K.of(chromosomes.get(1).gene().allele().doubleValue()),
                    layer3Extremal.hStep(),
                    new Model.P(chromosomes.get(2).gene().allele().intValue(), chromosomes.get(3).gene().allele().intValue())
                );
                return new Model.Layer3RelativeDh(layer3Relative, dpExtremal);
              },
              model -> {
                if (model instanceof Model.Layer3RelativeDh(
                    Model.Layer3Relative layer3, _
                )) {
                  return Genotype.of(
                      AnyChromosome.of(() -> layer3.k1().value()),
                      AnyChromosome.of(() -> layer3.k2().value()),
                      AnyChromosome.of(() -> layer3.p().p1()),
                      AnyChromosome.of(() -> layer3.p().p2mp1())
                  );
                }
                else {
                  throw new IllegalArgumentException("Unexpected value: " + model);
                }
              });
          case Model.Layer3Absolute layer3Absolute ->
              throw new IllegalArgumentException("Unexpected value: " + layer3Absolute);
          case Model.Layer3AbsoluteDh(Model.Layer3Absolute layer3Extremal, Model.P dpExtremal) -> InvertibleCodec.of(
              () -> Genotype.of(
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(1.35, layer3Extremal.rho1())),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(layer3Extremal.rho2(), 20.0)),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(1.35, layer3Extremal.rho3())),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(1, layer3Extremal.p().p1() + 1)),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(1, layer3Extremal.p().p2mp1() + 1)),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(0, dpExtremal.p1() + 1))
              ),
              chromosomes -> {
                Model.Layer3Absolute layer3Absolute = new Model.Layer3Absolute(
                    chromosomes.get(0).gene().allele().doubleValue(),
                    chromosomes.get(1).gene().allele().doubleValue(),
                    chromosomes.get(2).gene().allele().doubleValue(),
                    layer3Extremal.hStep(),
                    new Model.P(chromosomes.get(3).gene().allele().intValue(), chromosomes.get(4).gene().allele().intValue())
                );
                int p1 = chromosomes.get(5).gene().allele().intValue();
                int p2mp1 = dpExtremal.pSum() - p1;
                return new Model.Layer3AbsoluteDh(layer3Absolute, new Model.P(p1, p2mp1));
              },
              model -> {
                if (model instanceof Model.Layer3AbsoluteDh(
                    Model.Layer3Absolute layer3, Model.P dp
                )) {
                  return Genotype.of(
                      AnyChromosome.of(layer3::rho1),
                      AnyChromosome.of(layer3::rho2),
                      AnyChromosome.of(layer3::rho3),
                      AnyChromosome.of(() -> layer3.p().p1()),
                      AnyChromosome.of(() -> layer3.p().p2mp1()),
                      AnyChromosome.of(dp::p1)
                  );
                }
                else {
                  throw new IllegalArgumentException("Unexpected value: " + model);
                }
              });
        }, size);
      }

      private Solution innerSolve(Alpha alpha) {
        Cache<Model, Double> fitnessCache = Caffeine.newBuilder().maximumSize(size).build();
        LongAdder realEvaluationsCounter = new LongAdder();
        LongAdder totalEvaluationsCounter = new LongAdder();
        ToDoubleFunction<Model> fitness = model -> {
          totalEvaluationsCounter.increment();
          return fitnessCache.get(model, m -> {
            realEvaluationsCounter.increment();
            return DoubleStream.concat(
                    parametricFunctionals.stream().mapToDouble(f -> alpha.value * f.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG).applyAsDouble(m)),
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
          final double maxMutation = 1.0 / Math.E;
          final double minMutation = 0.01;
          final double decayFactor = 0.85; // Чем ближе к 1.0, тем медленнее спад

          for (int epoch = 0, totalEpochs = 1 << 4; epoch < totalEpochs; epoch++) {
            double mutationRate = minMutation + (maxMutation - minMutation) * StrictMath.pow(decayFactor, epoch);
            Engine<AnyGene<Number>, Double> engine = Engine.builder(fitness::applyAsDouble, modelFactory)
                .populationSize(size)
                .optimize(Optimize.MINIMUM)
                .executor(executor)
                .survivorsSelector(new EliteSelector<>(eliteSize))
                .selector(new TournamentSelector<>(parentSize))
                .offspringSelector(new TournamentSelector<>(offspringSize))
                .alterers(new Mutator<>(mutationRate), new UniformCrossover<>(0.6))
                .constraint(RetryConstraint.of(modelFactory, m -> Double.isFinite(fitness.applyAsDouble(m))))
                .build();

            EvolutionStream<AnyGene<Number>, Double> stream;
            if (evolutionState == null) {
              if (resetPopulation == null) {
                stream = engine.stream();
              }
              else {
                stream = engine.stream(resetPopulation);
              }
            }
            else {
              stream = engine.stream(evolutionState);
            }
            evolutionState = stream.limit(1 << 4).collect(EvolutionResult.toBestEvolutionResult());

            double bestFitness = evolutionState.bestFitness();
            Model currentBestInput = modelFactory.decode(evolutionState.bestPhenotype().genotype());

            LOGGER.atDebug().addKeyValue("Эпоха", "%02d/%02d".formatted(epoch + 1, totalEpochs))
                .addKeyValue("Поколение", "%03d".formatted(evolutionState.generation()))
                .addKeyValue("Мутация", "%04.1f %%".formatted(mutationRate * 100))
                .addKeyValue("Невязка", "%.4f".formatted(bestFitness))
                .log(currentBestInput::toString);

            if (bestFitness < 1.0E-6) {
              LOGGER.atDebug().log(() -> "--> Минимум найден досрочно!");
              break;
            }
          }
          resetPopulation = Objects.requireNonNull(evolutionState).population()
              .stream()
              .map(phenotype -> Phenotype.<AnyGene<Number>, Double>of(phenotype.genotype(), 0))
              .collect(ISeq.toISeq());
          LOGGER.atDebug()
              .addKeyValue("Вычислений", realEvaluationsCounter::sum)
              .addKeyValue("Всего попыток", totalEvaluationsCounter::sum)
              .addKeyValue("Экономия за счет кэша", () -> "%.0f %%".formatted((1.0 - realEvaluationsCounter.doubleValue() / totalEvaluationsCounter.sum()) * 100))
              .log(evolutionState.bestPhenotype().toString());
          Solution solution = new Solution(alpha, evolutionState.bestFitness(), modelFactory.decode(evolutionState.bestPhenotype().genotype()));
          LOGGER.atInfo().log(solution::toString);
          return solution;
        }
      }

      private Solution solve(double alpha) {
        return alphaCache.get(new Alpha(alpha), this::innerSolve);
      }

      @Override
      public Collection<Solution> solve(double... alphas) {
        return DoubleStream.of(alphas).mapToObj(this::solve).toList();
      }

      @Override
      public Solution solve() {
        Collection<Solution> solutions = solve(
            DoubleStream.concat(
                    Numbers.rangeLog(1.0 / 10.0, 1.0 / 0.001, 7).map(x -> 1.0 / x),
                    DoubleStream.of(0.0)
                )
                .toArray())
            .stream().sorted(Comparator.comparingDouble(Solution::fitness)).toList();

        UnivariateInterpolator interpolator = new AkimaSplineInterpolator(true);
        double[] x = solutions.stream().mapToDouble(Solution::fitness).toArray();
        double[] y = solutions.stream().mapToDouble(value -> value.alpha.value).toArray();

        double dataErrorNormBase = parametricFunctionals.stream().mapToDouble(ParametricFunctional::dataErrorNorm)
            .reduce(Math::hypot).orElseThrow();
        double dataErrorNormShift = solve(0.0).fitness();
        double dataErrorNorm = dataErrorNormBase + dataErrorNormShift;
        double alpha = interpolator.interpolate(x, y).value(dataErrorNorm);
        LOGGER.atInfo()
            .addKeyValue("data Error Norm Base", () -> "%.4f".formatted(dataErrorNormBase))
            .addKeyValue("data Error Norm Shift", () -> "%.4f".formatted(dataErrorNormShift))
            .addKeyValue("target fitness", () -> "%.4f".formatted(dataErrorNorm))
            .log(() -> new Alpha(alpha).toString());
        return solve(alpha);
      }
    }

    private final double base;
    private final Metrics.Length units;
    private final Collection<ParametricFunctional> parametricFunctionals = new ArrayList<>();
    private @Nullable Model origin;

    public SolverBuilder(double base, Metrics.Length units) {
      if (base > 0) {
        this.base = base;
      }
      else {
        throw new IllegalArgumentException("base = %f must be positive".formatted(base));
      }
      this.units = Objects.requireNonNull(units);
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
    public Step4<M> system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(5, 3, builderFunction);
      return this;
    }

    @Override
    public Step3 system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 4, builderFunction);
      return this;
    }

    @Override
    public Step5<M> system2x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(2, 4, builderFunction);
      return this;
    }

    @Override
    public Step3 system6x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(6, 4, builderFunction);
      return this;
    }

    @Override
    public Builder<Solver> origin(Function<Stream<ElectrodeSystem.Inexact>, Model> modelFunction) {
      origin = modelFunction.apply(parametricFunctionals.stream().map(ParametricFunctional::system));
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

    @Override
    public Solver build() {
      return new GeneticSolver(parametricFunctionals, Objects.requireNonNull(origin), 1 << 9);
    }
  }
}
