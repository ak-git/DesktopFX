package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import com.ak.util.Strings;
import org.testng.annotations.Test;

public class Electrode2LayerTest {
  private static final Logger LOGGER = Logger.getLogger(Electrode2LayerTest.class.getName());
  private static final double OVERALL_DIM = 0.001;
  private static final double ABS_ERROR_OVERALL_DIM = 1.0E-3 * OVERALL_DIM;

  @Test(enabled = false)
  public void test() {
    LineFileBuilder.<RelativeMediumLayers>of("%.3f %.3f %.6f")
        .xStream(() -> DoubleStream.of(0.5))
        .yRange(-1.0, 1.0, 0.1)
        .add("k1.txt", RelativeMediumLayers::k12)
        .add("h1.txt", RelativeMediumLayers::h)
        .generate((hToL, k) -> errorsScale(new double[] {0.2, 0.6}, k, hToL));

    LineFileBuilder.<RelativeMediumLayers>of("%.3f %.3f %.6f")
        .xStream(() -> DoubleStream.of(0.5))
        .yRange(-1.0, 1.0, 0.1)
        .add("k2.txt", RelativeMediumLayers::k12)
        .add("h2.txt", RelativeMediumLayers::h)
        .generate((hToL, k) -> errorsScale(new double[] {0.2, 0.6}, k, hToL,
            derivativeMeasurements -> Inverse.inverseStaticRelative(derivativeMeasurements, UnaryOperator.identity()))
        );
  }

  @Test(enabled = false)
  public void testSingle() {
    double hToDimMax = TetrapolarSystem.si().s(OVERALL_DIM / 3.0).l(OVERALL_DIM).getHMax(1.0, ABS_ERROR_OVERALL_DIM) / OVERALL_DIM;
    LOGGER.info(() -> errorsScale(new double[] {0.2, 0.6}, -1.0, hToDimMax).toString());
    LOGGER.info(() -> errorsScale(new double[] {0.2, 0.6}, -0.5, hToDimMax).toString());
    LOGGER.info(() -> errorsScale(new double[] {0.2, 0.6}, 0.5, hToDimMax).toString());
    LOGGER.info(() -> errorsScale(new double[] {0.2, 0.6}, 1.0, hToDimMax).toString());
  }

  @Test(enabled = false)
  public void testByH() {
    DoubleStream.iterate(0.2, h -> h < 1.0, h -> h += 0.2)
        .forEach(hToDim -> LOGGER.info(() -> errorsScale(new double[] {1.0 / 3.0, 5.0 / 3.0}, 1.0, hToDim).toString()));
  }

  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers errorsScale(double[] sToL, double k, @Nonnegative double hToDim) {
    return errorsScale(sToL, k, hToDim,
        derivativeMeasurements -> Inverse.inverseDynamicRelative(derivativeMeasurements,
            new RelativeMediumLayers() {
              @Override
              public double k12() {
                return k;
              }

              /**
               * @return h / L
               */
              @Override
              public double h() {
                return hToDim * Arrays.stream(sToL).reduce(1.0, Math::max);
              }

              @Override
              public String toString() {
                return "k%s%s = %+.3f; h/L = %.3f".formatted(Strings.low(1), Strings.low(2), k12(), h());
              }
            })
    );
  }

  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers errorsScale(double[] sToL, double k, @Nonnegative double hToDim,
                                                  Function<Collection<DerivativeMeasurement>, RelativeMediumLayers> inverse) {
    double maxRelDim = Arrays.stream(sToL).reduce(1.0, Math::max);
    DoubleUnaryOperator converterToAbs = rel -> OVERALL_DIM * rel / maxRelDim;
    double s1 = converterToAbs.applyAsDouble(sToL[0]);
    double s2 = converterToAbs.applyAsDouble(sToL[1]);
    double L = converterToAbs.applyAsDouble(1.0);

    InexactTetrapolarSystem[] systems = {
        InexactTetrapolarSystem.si(ABS_ERROR_OVERALL_DIM).s(s1).l(L),
        InexactTetrapolarSystem.si(ABS_ERROR_OVERALL_DIM).s(s2).l(L),
    };

    Logger logger = Logger.getLogger(Electrode2LayerTest.class.getName());
    return getTetrapolarSystemCombination(systems).stream()
        .peek(systemsError -> logger.config(
            () -> "s/L = [%.3f; %.3f]; k = %.3f; h/D = %.3f; %s"
                .formatted(s1 / L, s2 / L, k, hToDim, Arrays.deepToString(systemsError)))
        )
        .map(systemsError -> {
          Collection<DerivativeMeasurement> measurements = IntStream.range(0, systems.length)
              .mapToObj(i ->
                  new DerivativeMeasurement() {
                    private final DoubleUnaryOperator toResistivity = operand -> getSystem().toExact().getApparent(operand);

                    @Override
                    public double getDerivativeResistivity() {
                      return toResistivity.applyAsDouble(new NormalizedDerivativeR2ByH(systemsError[i]).value(k, hToL()));
                    }

                    @Override
                    public double getResistivity() {
                      return toResistivity.applyAsDouble(new NormalizedResistance2Layer(systemsError[i]).applyAsDouble(k, hToL()));
                    }

                    @Nonnull
                    @Override
                    public InexactTetrapolarSystem getSystem() {
                      return systems[i];
                    }

                    private double hToL() {
                      return hToDim * OVERALL_DIM / getSystem().toExact().getL();
                    }

                    @Override
                    public String toString() {
                      return "%s; h/L = %.3f; %s; %s".formatted(getSystem(), hToL(), Strings.rho(getResistivity()), Strings.dRhoByH(getDerivativeResistivity()));
                    }
                  })
              .collect(Collectors.toUnmodifiableList());
          return inverse.apply(measurements);
        })
        .peek(solution -> logger.config(
            () -> "s/L = [%.3f; %.3f]; k = %.3f; h/D = %.3f; k = %.3f; h = %.3f"
                .formatted(s1 / L, s2 / L, k, hToDim, solution.k12(), solution.h()))
        )
        .map(solution -> new RelativeMediumLayers() {
          @Override
          public double k12() {
            return Inequality.proportional().applyAsDouble(solution.k12(), k);
          }

          @Override
          public double h() {
            return Inequality.absolute().applyAsDouble(solution.h() / OVERALL_DIM, hToDim);
          }
        })
        .peek(errorFactors -> logger.config(
            () -> "s/L = [%.3f; %.3f]; k = %.3f; h/D = %.3f; \u03b4k = %.3f; \u0394(h/D) = %.3f"
                .formatted(s1 / L, s2 / L, k, hToDim, errorFactors.k12(), errorFactors.h()))
        )
        .parallel()
        .collect(
            Collectors.teeing(
                Collectors.maxBy(Comparator.comparingDouble(RelativeMediumLayers::k12)),
                Collectors.maxBy(Comparator.comparingDouble(RelativeMediumLayers::h)),
                (r1, r2) -> new RelativeMediumLayers() {
                  @Override
                  public double k12() {
                    return Math.max(r1.orElseThrow().k12(), r2.orElseThrow().k12());
                  }

                  @Override
                  public double h() {
                    return Math.max(r1.orElseThrow().h(), r2.orElseThrow().h());
                  }

                  @Override
                  public String toString() {
                    return "s/L = [%.3f; %.3f]; k = %.3f; h/D = %.3f; \u03b4k = %.3f; \u0394(h/D) = %.3f"
                        .formatted(s1 / L, s2 / L, k, hToDim, k12(), h());
                  }
                }
            )
        );
  }

  @Nonnull
  private static Collection<TetrapolarSystem[]> getTetrapolarSystemCombination(@Nonnull InexactTetrapolarSystem[] systems) {
    return IntStream.range(0, 8)
        .mapToObj(n -> {
          int signS1 = (n & 1) == 0 ? 1 : -1;
          int signL = (n & 2) == 0 ? 1 : -1;
          int signS2 = (n & 4) == 0 ? 1 : -1;

          return new TetrapolarSystem[] {
              systems[0].shift(signS1, signL),
              systems[1].shift(signS2, signL)
          };
        })
        .collect(Collectors.toUnmodifiableList());
  }
}
