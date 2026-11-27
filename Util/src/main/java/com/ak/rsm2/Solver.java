package com.ak.rsm2;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import org.apache.commons.math4.legacy.analysis.MultivariateFunction;
import org.apache.commons.math4.legacy.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math4.legacy.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math4.legacy.exception.TooManyEvaluationsException;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.SimpleValueChecker;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.NelderMeadTransform;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.Simplex;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.UnaryOperator;
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

  record Fitness(double value, double misfit) {
    public static final Fitness POSITIVE_INFINITY = new Fitness(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    public Fitness {
      if (value < 0) {
        throw new IllegalArgumentException("value %f < 0".formatted(value));
      }
      if (misfit < 0) {
        throw new IllegalArgumentException("misfit %f < 0".formatted(misfit));
      }
    }

    @Override
    public String toString() {
      return "Fitness{%.4f; misfit = %.4f}".formatted(value, misfit);
    }
  }

  record Solution(Alpha alpha, Fitness fitness, Model model) {
    public Solution {
      Objects.requireNonNull(alpha);
      Objects.requireNonNull(fitness);
      Objects.requireNonNull(model);
    }

    @Override
    public String toString() {
      return "Solution{%s; %s; %s}".formatted(alpha, fitness, model);
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
    private static final double STOP_FITNESS = 1.0e-6;

    sealed interface FitnessProvider extends Function<Alpha, Function<Model, Fitness>> {
      double dataErrorNormBase();
    }

    private record FitnessProviderRecord(Collection<ParametricFunctional> parametricFunctionals,
                                         double dataErrorNormBase) implements FitnessProvider {
      private FitnessProviderRecord {
        Objects.requireNonNull(parametricFunctionals);
        if (dataErrorNormBase < 0) {
          throw new IllegalArgumentException("%f".formatted(dataErrorNormBase));
        }
      }

      private FitnessProviderRecord(Collection<ParametricFunctional> parametricFunctionals) {
        this(parametricFunctionals, parametricFunctionals.stream().mapToDouble(ParametricFunctional::dataErrorNorm)
            .reduce(Math::hypot).orElseThrow());
      }

      @Override
      public Function<Model, Fitness> apply(Alpha alpha) {
        Cache<Model, Fitness> fitnessCache = Caffeine.newBuilder().maximumSize(1 << 16).build();
        return model -> fitnessCache.get(model, m -> {
          double stab = parametricFunctionals.stream()
              .mapToDouble(f -> f.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG).applyAsDouble(m))
              .sum();
          if (Double.isFinite(stab)) {
            double misfit = parametricFunctionals.stream()
                .mapToDouble(f -> f.misfit().applyAsDouble(m)).reduce(StrictMath::hypot).orElseThrow();
            if (Double.isFinite(misfit)) {
              double value = StrictMath.hypot(misfit, StrictMath.sqrt(alpha.value * stab));
              return new Fitness(value, misfit);
            }
          }
          return Fitness.POSITIVE_INFINITY;
        });
      }
    }

    private record NelderMeadRefiner(Model origin,
                                     Function<Model, Fitness> fitness) implements UnaryOperator<Solution> {
      private static final Logger LOGGER = LoggerFactory.getLogger(NelderMeadRefiner.class);
      private static final int MAX_SIMPLEX_EVALUATIONS = 1 << 8;

      private NelderMeadRefiner(Model origin, Function<Model, Fitness> fitness) {
        this.origin = Objects.requireNonNull(origin);
        this.fitness = Objects.requireNonNull(fitness);
      }

      @Override
      public Solution apply(Solution current) {
        Model model = switch (current.model) {
          case Model.Layer2Relative(K k, double h) when origin instanceof Model.Layer2Relative(K kExt, double hExt) -> {
            double kMin = Math.min(kExt.value(), 0);
            double kMax = Math.max(0, kExt.value());
            double[] init = {k.value(), h};
            double[] steps = {
                directedStep(init[0], kMin, kMax, 0.05),
                directedStep(init[1], 0, hExt, hExt / 50.0)
            };
            double[] opt = optimizeNelderMead(point -> {
              if (point[0] < kMin || point[0] > kMax || point[1] < 0 || point[1] > hExt) {
                return Double.POSITIVE_INFINITY;
              }
              return fitness.apply(new Model.Layer2Relative(K.of(point[0]), point[1])).value;
            }, init, steps);
            yield new Model.Layer2Relative(K.of(opt[0]), opt[1]);
          }
          case Model.Layer2RelativeDh(Model.Layer2Relative(K k, double h), double dh)
              when origin instanceof Model.Layer2RelativeDh(Model.Layer2Model l2Ext, double dhExt) -> {
            double kMin = Math.min(l2Ext.k().value(), 0);
            double kMax = Math.max(0, l2Ext.k().value());
            double dhMin = Math.min(dhExt, 0);
            double dhMax = Math.max(0, dhExt);
            double[] init = {k.value(), h, dh};
            double[] steps = {
                directedStep(init[0], kMin, kMax, 0.05),
                directedStep(init[1], 0, l2Ext.h(), l2Ext.h() / 50.0),
                directedStep(init[2], dhMin, dhMax, Math.abs(dhExt) / 50.0)
            };
            double[] opt = optimizeNelderMead(point -> {
              if (point[0] < kMin || point[0] > kMax ||
                  point[1] < 0 || point[1] > l2Ext.h() ||
                  point[2] < dhMin || point[2] > dhMax) {
                return Double.POSITIVE_INFINITY;
              }
              return fitness.apply(new Model.Layer2RelativeDh(K.of(point[0]), point[1], point[2])).value;
            }, init, steps);
            yield new Model.Layer2RelativeDh(K.of(opt[0]), opt[1], opt[2]);
          }
          case Model.Layer3RelativeDh(Model.Layer3Relative(K k1, K k2, double hStep, Model.P p), Model.P dp)
              when origin instanceof Model.Layer3RelativeDh(Model.Layer3Relative l3Ext, _) -> {
            double k1Min = Math.min(l3Ext.k1().value(), 0);
            double k1Max = Math.max(0, l3Ext.k1().value());
            double k2Min = Math.min(l3Ext.k2().value(), 0);
            double k2Max = Math.max(0, l3Ext.k2().value());
            double[] init = {k1.value(), k2.value()};
            double[] steps = {
                directedStep(init[0], k1Min, k1Max, 0.05),
                directedStep(init[1], k2Min, k2Max, 0.05)
            };
            double[] opt = optimizeNelderMead(point -> {
              if (point[0] < k1Min || point[0] > k1Max ||
                  point[1] < k2Min || point[1] > k2Max) {
                return Double.POSITIVE_INFINITY;
              }
              return fitness.apply(new Model.Layer3RelativeDh(
                  new Model.Layer3Relative(K.of(point[0]), K.of(point[1]), hStep, p), dp)).value;
            }, init, steps);
            yield new Model.Layer3RelativeDh(new Model.Layer3Relative(K.of(opt[0]), K.of(opt[1]), hStep, p), dp);
          }
          case Model.Layer3AbsoluteDh(
              Model.Layer3Absolute(double rho1, double rho2, double rho3, double hStep, Model.P p), Model.P dp
          )
              when origin instanceof Model.Layer3AbsoluteDh(Model.Layer3Absolute l3Ext, _) -> {
            double[] init = {rho1, rho2, rho3};
            double[] steps = {
                directedStep(init[0], 1.35, l3Ext.rho1(), 0.1),
                directedStep(init[1], l3Ext.rho2(), 20.0, 0.1),
                directedStep(init[2], 1.35, l3Ext.rho3(), 0.1)
            };
            double[] opt = optimizeNelderMead(point -> {
              if (point[0] < 1.35 || point[0] > l3Ext.rho1() ||
                  point[1] < l3Ext.rho2() || point[1] > 20.0 ||
                  point[2] < 1.35 || point[2] > l3Ext.rho3()) {
                return Double.POSITIVE_INFINITY;
              }
              return fitness.apply(new Model.Layer3AbsoluteDh(
                  new Model.Layer3Absolute(point[0], point[1], point[2], hStep, p), dp)).value;
            }, init, steps);
            yield new Model.Layer3AbsoluteDh(new Model.Layer3Absolute(opt[0], opt[1], opt[2], hStep, p), dp);
          }
          default -> current.model;
        };
        return new Solution(current.alpha, fitness.apply(model), model);
      }

      private static double directedStep(double value, double min, double max, double step) {
        double s = Math.min(step, (max - min) / 10.0);
        if (s <= 0.0) {
          return 1.0e-6;
        }
        return (value + s > max) ? -s : s;
      }

      private static double[] optimizeNelderMead(MultivariateFunction function, double[] initialGuess, double[] initialSteps) {
        double[] bestPoint = initialGuess.clone();
        double[] bestValue = {Double.POSITIVE_INFINITY};

        MultivariateFunction trackedFunction = point -> {
          double val = function.value(point);
          if (val < bestValue[0]) {
            bestValue[0] = val;
            System.arraycopy(point, 0, bestPoint, 0, point.length);
          }
          return val;
        };

        try {
          PointValuePair pair = new SimplexOptimizer(
              new SimpleValueChecker(STOP_FITNESS * 10.0, STOP_FITNESS)
          ).optimize(
              new MaxEval(MAX_SIMPLEX_EVALUATIONS),
              new ObjectiveFunction(trackedFunction),
              GoalType.MINIMIZE,
              Simplex.alongAxes(initialSteps),
              new NelderMeadTransform(),
              new InitialGuess(initialGuess)
          );
          return pair.getPoint();
        }
        catch (TooManyEvaluationsException e) {
          LOGGER.atDebug().log(e::getMessage);
          return bestPoint;
        }
      }
    }

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
      private final FitnessProvider fitnessProvider;
      private final Model origin;
      private final InvertibleCodec<Model, AnyGene<Number>> modelFactory;
      private final Cache<Alpha, Solution> alphaCache = Caffeine.newBuilder().maximumSize(1 << 6).build();
      private @Nullable ISeq<Phenotype<AnyGene<Number>, Double>> resetPopulation;

      private GeneticSolver(FitnessProvider fitnessProvider, Model origin, int size) {
        this.fitnessProvider = Objects.requireNonNull(fitnessProvider);
        this.origin = origin;
        this.size = size;
        eliteSize = Math.max(3, (int) Math.floor(size * 0.006));
        parentSize = Math.max(2, (int) (StrictMath.log(size) / StrictMath.log(2.0)) - 5);
        offspringSize = Math.max(2, (int) Math.floor(parentSize * 0.6) + 1);
        modelFactory = switch (origin) {
          case Model.Layer2Relative(K kExtremal, double hExtremal) -> InvertibleCodec.of(
              () -> Genotype.of(
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(Math.min(kExtremal.value(), 0), Math.max(0, kExtremal.value()))),
                  AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(0, hExtremal))
              ),
              chromosomes -> new Model.Layer2Relative(
                  K.of(round3(chromosomes.get(0).gene().allele().doubleValue())),
                  round3(chromosomes.get(1).gene().allele().doubleValue())
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
                  K.of(round3(chromosomes.get(0).gene().allele().doubleValue())),
                  round3(chromosomes.get(1).gene().allele().doubleValue()),
                  round3(chromosomes.get(2).gene().allele().doubleValue())
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
                    K.of(round3(chromosomes.get(0).gene().allele().doubleValue())),
                    K.of(round3(chromosomes.get(1).gene().allele().doubleValue())),
                    layer3Extremal.hStep(),
                    new Model.P(chromosomes.get(2).gene().allele().intValue(), chromosomes.get(3).gene().allele().intValue())
                );
                return new Model.Layer3RelativeDh(layer3Relative, dpExtremal);
              },
              model -> {
                if (model instanceof Model.Layer3RelativeDh(Model.Layer3Relative layer3, _)) {
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
                    round3(chromosomes.get(0).gene().allele().doubleValue()),
                    round3(chromosomes.get(1).gene().allele().doubleValue()),
                    round3(chromosomes.get(2).gene().allele().doubleValue()),
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
          default -> throw new IllegalStateException("Unexpected value: " + origin);
        };
      }

      private static double round3(double val) {
        return Math.round(val * 1000.0) / 1000.0;
      }

      private Solution innerSolve(Alpha alpha) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
          Function<Model, Fitness> fitness = fitnessProvider.apply(alpha);
          EvolutionResult<AnyGene<Number>, Double> evolutionState = null;
          Solution bestSolution = new Solution(alpha, Fitness.POSITIVE_INFINITY, new Model.Empty());
          Solution lastRefinedSolution = new Solution(alpha, Fitness.POSITIVE_INFINITY, new Model.Empty());
          double previousEpochFitness = Double.POSITIVE_INFINITY;
          int stagnantEpochs = 0;
          final double maxMutation = 1.0 / Math.E;
          final double minMutation = 0.01;
          final double decayFactor = 0.85;
          NelderMeadRefiner refiner = new NelderMeadRefiner(origin, fitness);

          for (int epoch = 0, totalEpochs = 1 << 4; epoch < totalEpochs; epoch++) {
            double mutationRate = minMutation + (maxMutation - minMutation) * StrictMath.pow(decayFactor, epoch);
            Engine<AnyGene<Number>, Double> engine = Engine.builder(model -> fitness.apply(model).value, modelFactory)
                .populationSize(size)
                .optimize(Optimize.MINIMUM)
                .executor(executor)
                .survivorsSelector(new EliteSelector<>(eliteSize))
                .selector(new TournamentSelector<>(parentSize))
                .offspringSelector(new TournamentSelector<>(offspringSize))
                .alterers(new Mutator<>(mutationRate), new UniformCrossover<>(0.6))
                .constraint(RetryConstraint.of(modelFactory, m -> Double.isFinite(fitness.apply(m).value)))
                .build();

            EvolutionStream<AnyGene<Number>, Double> stream;
            if (evolutionState == null) {
              stream = resetPopulation == null ? engine.stream() : engine.stream(resetPopulation);
            }
            else {
              stream = engine.stream(evolutionState);
            }
            evolutionState = stream.limit(1 << 6).collect(EvolutionResult.toBestEvolutionResult());

            Model bestPhenotype = modelFactory.decode(evolutionState.bestPhenotype().genotype());
            Solution epochSolution = new Solution(alpha, fitness.apply(bestPhenotype), bestPhenotype);
            if (epochSolution.fitness.value < bestSolution.fitness.value) {
              bestSolution = epochSolution;
            }

            if (!epochSolution.equals(lastRefinedSolution)) {
              lastRefinedSolution = epochSolution;
              Solution refined = refiner.apply(epochSolution);
              if (refined.fitness.value < bestSolution.fitness.value) {
                bestSolution = refined;
              }
            }

            if (previousEpochFitness - bestSolution.fitness.value < STOP_FITNESS) {
              stagnantEpochs++;
            }
            else {
              stagnantEpochs = 0;
              previousEpochFitness = bestSolution.fitness.value;
            }

            LOGGER.atDebug().addKeyValue("Эпоха", "%02d/%02d".formatted(epoch + 1, totalEpochs))
                .addKeyValue("Поколение", "%03d".formatted(evolutionState.generation()))
                .addKeyValue("Мутация", "%04.1f %%".formatted(mutationRate * 100))
                .log(Objects.requireNonNull(bestSolution)::toString);

            if (bestSolution.fitness.value < STOP_FITNESS || (epoch > totalEpochs / 2 - 1 && stagnantEpochs > 2)) {
              break;
            }
          }

          Phenotype<AnyGene<Number>, Double> refinedPhenotype = Phenotype.of(modelFactory.encode(bestSolution.model()), 0);
          resetPopulation = Stream.concat(
                  Stream.of(refinedPhenotype),
                  Objects.requireNonNull(evolutionState).population().stream()
                      .map(phenotype -> Phenotype.<AnyGene<Number>, Double>of(phenotype.genotype(), 0))
              )
              .limit(size)
              .collect(ISeq.toISeq());

          LOGGER.atInfo().log(bestSolution::toString);
          return bestSolution;
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
            .stream().sorted(Comparator.comparingDouble(s -> s.fitness.value)).toList();

        UnivariateInterpolator interpolator = new AkimaSplineInterpolator(true);
        double[] x = solutions.stream().mapToDouble(s -> s.fitness.misfit).toArray();
        double[] y = solutions.stream().mapToDouble(s -> s.alpha.value).toArray();

        double dataErrorNormBase = fitnessProvider.dataErrorNormBase();
        double dataErrorNormShift = solve(0.0).fitness().misfit;
        double dataErrorNorm = dataErrorNormBase + dataErrorNormShift;
        double alpha = interpolator.interpolate(x, y).value(dataErrorNorm);
        LOGGER.atInfo()
            .addKeyValue("data Error Norm Base", () -> "%.4f".formatted(dataErrorNormBase))
            .addKeyValue("data Error Norm Shift", () -> "%.4f".formatted(dataErrorNormShift))
            .addKeyValue("target misfit", () -> "%.4f".formatted(dataErrorNorm))
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
      return new GeneticSolver(new FitnessProviderRecord(parametricFunctionals), Objects.requireNonNull(origin), 1 << 8);
    }
  }
}
