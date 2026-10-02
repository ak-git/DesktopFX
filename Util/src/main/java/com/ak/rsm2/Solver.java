package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.NelderMeadTransform;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.jspecify.annotations.Nullable;
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
import java.util.function.DoubleUnaryOperator;
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
      return "alpha = %.5f".formatted(value);
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

  Solution solve(double alpha);

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
    Step3 system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);

    Step3 system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction);
  }

  sealed interface Step3 {
    Builder<Solver> origin(Function<Stream<ElectrodeSystem.Inexact>, Model> modelFunction);
  }

  final class SolverBuilder<M extends TetrapolarMeasurement> implements Step1<M>, Step2<M>, Step3, Builder<Solver> {
    sealed interface Scaler {
      static int toInt(Genotype<IntegerGene> genotype, int chromosome) {
        return genotype.get(chromosome).as(IntegerChromosome.class).gene().allele();
      }


      IntRange range();

      double toSI(int index);

      default double toSI(Genotype<IntegerGene> genotype, int chromosome) {
        return toSI(toInt(genotype, chromosome));
      }

      int toIndex(double x);

      record KScaler(IntRange range) implements Scaler {
        private static final int SCALE = 1000;

        public KScaler(K kExtremal) {
          int index = index(Math.clamp(kExtremal.value(), -1.0, 1.0));
          this(new IntRange(Math.min(index, 0), Math.max(0, index)));
        }

        @Override
        public double toSI(int index) {
          return Math.clamp(index * 1.0 / SCALE, -1.0, 1.0);
        }

        @Override
        public int toIndex(double k) {
          return index(k);
        }

        private static int index(double k) {
          return Math.clamp(Numbers.toInt(k * SCALE), -SCALE, SCALE);
        }
      }

      record HScaler(IntRange range) implements Scaler {
        public HScaler(double hExtremal) {
          int index = index(hExtremal);
          this(new IntRange(Math.min(index, 0), Math.max(0, index)));
        }

        @Override
        public double toSI(int index) {
          return Metrics.Length.MICRO.toSI(index);
        }

        @Override
        public int toIndex(double hSI) {
          return index(hSI);
        }

        private static int index(double hSI) {
          return Numbers.toInt(Metrics.Length.METRE.to(hSI, MetricPrefix.MICRO(Units.METRE)));
        }
      }

      record RhoScalerUpTo(IntRange range) implements Scaler {
        private static final int SCALE = 1000;

        public RhoScalerUpTo(double rhoMax) {
          if (rhoMax < 2.0) {
            throw new IllegalArgumentException("rho = %f < 2.0".formatted(rhoMax));
          }
          this(new IntRange(index(1.5), index(rhoMax)));
        }

        @Override
        public double toSI(int index) {
          return index * 1.0 / SCALE;
        }

        @Override
        public int toIndex(double rho) {
          return index(rho);
        }

        private static int index(double rho) {
          return Numbers.toInt(rho * SCALE);
        }
      }

      record RhoScalerAtLeast(IntRange range) implements Scaler {
        private static final int SCALE = 1000;

        public RhoScalerAtLeast(double rhoMin) {
          if (rhoMin > 20.0) {
            throw new IllegalArgumentException("rho = %f > 20.0".formatted(rhoMin));
          }
          this(new IntRange(index(rhoMin), index(20.0)));
        }

        @Override
        public double toSI(int index) {
          return index * 1.0 / SCALE;
        }

        @Override
        public int toIndex(double rho) {
          return index(rho);
        }

        private static int index(double rho) {
          return Numbers.toInt(rho * SCALE);
        }
      }

      record DRhoScaler(IntRange range) implements Scaler {
        private static final int SCALE = 1000;

        public DRhoScaler(double dRhoExtremal) {
          if (Math.abs(dRhoExtremal) > 1.0) {
            throw new IllegalArgumentException("|dRho = %f| > 0.1".formatted(dRhoExtremal));
          }
          int max = index(1.0);
          int index = index(dRhoExtremal);
          this(new IntRange(Math.min(-max, index), Math.max(index, max)));
        }

        @Override
        public double toSI(int index) {
          return index * 1.0 / SCALE;
        }

        @Override
        public int toIndex(double rho) {
          return index(rho);
        }

        private static int index(double rho) {
          return Numbers.toInt(rho * SCALE);
        }
      }
    }

    private static final class GeneticSolver implements Solver {
      private static final Logger LOGGER = LoggerFactory.getLogger(GeneticSolver.class);
      private static final int SIZE = 1 << 7;
      /**
       * Элита измеряется долей от популяции.
       * Эмпирическое правило для сложных многопараметрических задач: элита должна составлять от 0.5% до 1% от размера популяции.
       * При N = 1024 -> 1024 * 0.006 = 6 особей.
       * При N = 4096 -> 4096 * 0.006 = 25 особей.
       */
      private static final int ELITE_SIZE = Math.max(3, (int) Math.floor(SIZE * 0.006));
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
      private static final int PARENT_TOURNAMENT = Math.max(2, (int) (StrictMath.log(SIZE) / StrictMath.log(2.0)) - 5);
      /**
       * Мягкий турнирный селектор для оставшейся части выживающих особей.
       * Селектор потомков всегда должен быть мягче, чем родительский, чтобы сохранять новые мутации.
       * На практике его размер берут как половину или около того от турнира родителей, но не ниже двух.
       * Формула обеспечивает плавный шаг отставания от родительского турнира:
       * При k = 5 : 4 особи.
       * При k = 7 : 5 особей.
       */
      private static final int OFFSPRING_TOURNAMENT = Math.max(2, (int) Math.floor(PARENT_TOURNAMENT * 0.6) + 1);
      private final Collection<ParametricFunctional> parametricFunctionals;
      private final InvertibleCodec<Model, IntegerGene> modelFactory;
      private final Cache<Alpha, Solution> alphaCache = Caffeine.newBuilder().maximumSize(1 << 6).build();
      private @Nullable ISeq<Phenotype<IntegerGene, Double>> resetPopulation;

      private GeneticSolver(Collection<ParametricFunctional> parametricFunctionals, InvertibleCodec<Model, IntegerGene> modelFactory) {
        this.parametricFunctionals = Objects.requireNonNull(parametricFunctionals);
        this.modelFactory = Objects.requireNonNull(modelFactory);
      }

      GeneticSolver(Collection<ParametricFunctional> parametricFunctionals, Model origin) {
        this(parametricFunctionals, switch (origin) {
          case Model.Layer2Relative(K kExtremal, double hExtremal) -> {
            Scaler.KScaler kScaler = new Scaler.KScaler(kExtremal);
            Scaler.HScaler hScaler = new Scaler.HScaler(hExtremal);
            yield InvertibleCodec.of(
                () -> Genotype.of(
                    IntegerChromosome.of(kScaler.range),
                    IntegerChromosome.of(hScaler.range)
                ),
                chromosomes -> new Model.Layer2Relative(
                    K.of(kScaler.toSI(chromosomes, 0)),
                    hScaler.toSI(chromosomes, 1)
                ),
                model -> {
                  if (model instanceof Model.Layer2Relative(K k, double h)) {
                    return Genotype.of(
                        IntegerChromosome.of(IntegerGene.of(kScaler.toIndex(k.value()), kScaler.range)),
                        IntegerChromosome.of(IntegerGene.of(hScaler.toIndex(h), hScaler.range))
                    );
                  }
                  else {
                    throw new IllegalStateException("Unexpected value: " + model);
                  }
                });
          }
          case Model.Layer2RelativeDh(Model.Layer2Relative layer2RelativeExtremal, double dhExtremal) -> {
            Scaler.KScaler kScaler = new Scaler.KScaler(layer2RelativeExtremal.k());
            Scaler.HScaler hScaler = new Scaler.HScaler(layer2RelativeExtremal.h());
            Scaler.HScaler dhScaler = new Scaler.HScaler(dhExtremal);
            yield InvertibleCodec.of(
                () -> Genotype.of(
                    IntegerChromosome.of(kScaler.range),
                    IntegerChromosome.of(hScaler.range),
                    IntegerChromosome.of(dhScaler.range)
                ),
                chromosomes -> new Model.Layer2RelativeDh(
                    K.of(kScaler.toSI(chromosomes, 0)),
                    hScaler.toSI(chromosomes, 1),
                    dhScaler.toSI(chromosomes, 2)
                ),
                model -> {
                  if (model instanceof Model.Layer2RelativeDh(Model.Layer2Relative layer2Relative, double dh)) {
                    return Genotype.of(
                        IntegerChromosome.of(IntegerGene.of(kScaler.toIndex(layer2Relative.k().value()), kScaler.range)),
                        IntegerChromosome.of(IntegerGene.of(hScaler.toIndex(layer2Relative.h()), hScaler.range)),
                        IntegerChromosome.of(IntegerGene.of(dhScaler.toIndex(dh), dhScaler.range))
                    );
                  }
                  else {
                    throw new IllegalStateException("Unexpected value: " + model);
                  }
                });
          }
          case Model.Layer3Absolute layer3Absolute -> throw new IllegalArgumentException(layer3Absolute.toString());
          case Model.Layer3AbsoluteDRho2(
              Model.Layer3Absolute layer3Extremal, Model.P dpExtremal, double dRho2Extremal
          ) -> {
            Scaler.RhoScalerUpTo rho1Scaler = new Scaler.RhoScalerUpTo(layer3Extremal.rho1());
            Scaler.RhoScalerAtLeast rho2Scaler = new Scaler.RhoScalerAtLeast(layer3Extremal.rho2());
            Scaler.RhoScalerUpTo rho3Scaler = new Scaler.RhoScalerUpTo(layer3Extremal.rho3());
            Scaler.DRhoScaler dRhoScaler = new Scaler.DRhoScaler(dRho2Extremal);
            IntRange p1Range = new IntRange(1, layer3Extremal.p().p1());
            IntRange p2mp1Range = new IntRange(1, layer3Extremal.p().p2mp1());
            IntRange dp1Range = new IntRange(0, dpExtremal.p1());
            IntRange dp2mp1Range = new IntRange(0, dpExtremal.p2mp1());
            yield InvertibleCodec.of(
                () -> Genotype.of(
                    IntegerChromosome.of(rho1Scaler.range),
                    IntegerChromosome.of(rho2Scaler.range),
                    IntegerChromosome.of(rho3Scaler.range),
                    IntegerChromosome.of(p1Range),
                    IntegerChromosome.of(p2mp1Range),
                    IntegerChromosome.of(dp1Range),
                    IntegerChromosome.of(dp2mp1Range),
                    IntegerChromosome.of(dRhoScaler.range)
                ),
                chromosomes -> {
                  Model.Layer3Absolute layer3Absolute = new Model.Layer3Absolute(
                      rho1Scaler.toSI(chromosomes, 0),
                      rho2Scaler.toSI(chromosomes, 1),
                      rho3Scaler.toSI(chromosomes, 2),
                      layer3Extremal.hStep(),
                      new Model.P(Scaler.toInt(chromosomes, 3), Scaler.toInt(chromosomes, 4))
                  );
                  return new Model.Layer3AbsoluteDRho2(layer3Absolute,
                      new Model.P(Scaler.toInt(chromosomes, 5), Scaler.toInt(chromosomes, 6)),
                      dRhoScaler.toSI(chromosomes, 7));
                },
                model -> {
                  if (model instanceof Model.Layer3AbsoluteDRho2(
                      Model.Layer3Absolute layer3, Model.P dp, double dRho2
                  )) {
                    return Genotype.of(
                        IntegerChromosome.of(IntegerGene.of(rho1Scaler.toIndex(layer3.rho1()), rho1Scaler.range)),
                        IntegerChromosome.of(IntegerGene.of(rho2Scaler.toIndex(layer3.rho2()), rho2Scaler.range)),
                        IntegerChromosome.of(IntegerGene.of(rho3Scaler.toIndex(layer3.rho3()), rho3Scaler.range)),
                        IntegerChromosome.of(IntegerGene.of(layer3.p().p1(), p1Range)),
                        IntegerChromosome.of(IntegerGene.of(layer3.p().p2mp1(), p2mp1Range)),
                        IntegerChromosome.of(IntegerGene.of(dp.p1(), dp1Range)),
                        IntegerChromosome.of(IntegerGene.of(dp.p2mp1(), dp2mp1Range)),
                        IntegerChromosome.of(IntegerGene.of(dRhoScaler.toIndex(dRho2), dRhoScaler.range))
                    );
                  }
                  else {
                    throw new IllegalStateException("Unexpected value: " + model);
                  }
                });
          }
        });
      }

      private Solution innerSolve(Alpha alpha) {
        Cache<Model, Double> fitnessCache = Caffeine.newBuilder().maximumSize(SIZE).build();
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

        EvolutionResult<IntegerGene, Double> evolutionState = null;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
          for (int epoch = 0, totalEpochs = 1 << 4; epoch < totalEpochs; epoch++) {
            double mutationRate = Math.clamp(1.0 / Math.E / Integer.highestOneBit(epoch), 0.01, 1.0 / Math.E);
            Engine<IntegerGene, Double> engine = Engine.builder(fitness::applyAsDouble, modelFactory)
                .populationSize(SIZE)
                .optimize(Optimize.MINIMUM)
                .executor(executor)
                .survivorsSelector(new EliteSelector<>(ELITE_SIZE))
                .selector(new TournamentSelector<>(PARENT_TOURNAMENT))
                .offspringSelector(new TournamentSelector<>(OFFSPRING_TOURNAMENT))
                .alterers(new Mutator<>(mutationRate), new LineCrossover<>(0.15), new MultiPointCrossover<>(0.6, 2))
                .constraint(RetryConstraint.of(modelFactory, m -> Double.isFinite(fitness.applyAsDouble(m))))
                .build();

            EvolutionStream<IntegerGene, Double> stream;
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
              .map(phenotype -> Phenotype.<IntegerGene, Double>of(phenotype.genotype(), 0)) // <-- ЯВНО УКАЗАЛИ ТИПЫ ХЕЛПЕРУ
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

      @Override
      public Solution solve(double alpha) {
        return alphaCache.get(new Alpha(alpha), this::innerSolve);
      }

      @Override
      public Collection<Solution> solve(double... alphas) {
        return DoubleStream.of(alphas).mapToObj(this::solve).toList();
      }

      @Override
      public Solution solve() {
        Alpha initialAlpha = new Alpha(0.001);
        Collection<Solution> solutions = solve(initialAlpha.value * 10_000, initialAlpha.value * 100, initialAlpha.value);

        double dataErrorNormBase = parametricFunctionals.stream().mapToDouble(ParametricFunctional::dataErrorNorm).reduce(Math::hypot).orElseThrow();
        double dataErrorNormShift = solve(0.0).fitness();
        double dataErrorNorm = dataErrorNormBase + dataErrorNormShift;
        LOGGER.atInfo()
            .addKeyValue("data Error Norm Base", () -> "%.4f".formatted(dataErrorNormBase))
            .addKeyValue("data Error Norm Shift (alpha = 0)", () -> "%.4f".formatted(dataErrorNormShift))
            .log(() -> "target fitness = %.4f".formatted(dataErrorNorm));

        initialAlpha = solutions.stream().min(Comparator.comparingDouble(s -> Math.abs(s.fitness() - dataErrorNorm)))
            .map(Solution::alpha).orElse(initialAlpha);

        DoubleUnaryOperator withAlpha = alpha -> {
          if (alpha < 0) {
            return Double.POSITIVE_INFINITY;
          }
          else {
            Solution solution = solve(alpha);
            LOGGER.atInfo().log(solution::toString);
            double v = solution.fitness() - dataErrorNorm;
            return v * v;
          }
        };

        PointValuePair optimized = new SimplexOptimizer(0.000_000_1, 0.000_01)
            .optimize(
                new MaxEval(100), new ObjectiveFunction(point -> withAlpha.applyAsDouble(point[0])), GoalType.MINIMIZE,
                org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.Simplex.alongAxes(new double[] {initialAlpha.value}),
                new NelderMeadTransform(), new InitialGuess(new double[] {initialAlpha.value})
            );
        double alpha = optimized.getPoint()[0];
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
    public Step3 system5x3(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(5, 3, builderFunction);
      return this;
    }

    @Override
    public Step3 system1x4(Function<TetrapolarMeasurement.Step1, Builder<M>> builderFunction) {
      addSystem(1, 4, builderFunction);
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
      return new GeneticSolver(parametricFunctionals, Objects.requireNonNull(origin));
    }
  }
}
