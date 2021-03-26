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
import org.testng.Assert;
import org.testng.annotations.Test;

public class Electrode2LayerTest {
  private static final Logger LOGGER = Logger.getLogger(Electrode2LayerTest.class.getName());
  private static final double OVERALL_DIM = 1.0;
  private static final double REL_ERROR_OVERALL_DIM = 1.0e-5;
  private static final double ABS_ERROR_OVERALL_DIM = REL_ERROR_OVERALL_DIM * OVERALL_DIM;

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

  @Test
  public void testSingle() {
    var errorsScale = errorsScale(new double[] {10.0 / 30.0, 50.0 / 30.0}, Layers.getK12(1.0, 4.0), 10.0 / 50.0);
    Assert.assertEquals(errorsScale.k12(), 10.0, 0.1, errorsScale.toString());
    Assert.assertEquals(errorsScale.h(), 2.0, 0.1, errorsScale.toString());

    errorsScale = errorsScale(new double[] {10.0 / 30.0, 30.0 / 50.0}, Layers.getK12(1.0, 4.0), 10.0 / 50.0);
    Assert.assertEquals(errorsScale.k12(), 5.7, 0.1, errorsScale.toString());
    Assert.assertEquals(errorsScale.h(), 1.6, 0.1, errorsScale.toString());
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

    return InexactTetrapolarSystem.getTetrapolarSystemCombination(systems).stream()
        .peek(systemsError -> LOGGER.config(
            () -> "s/L = [%.3f; %.3f]; k = %.3f; h/D = %.3f; %s"
                .formatted(s1 / L, s2 / L, k, hToDim, Arrays.deepToString(systemsError)))
        )
        .map(systemsError -> {
          Collection<DerivativeMeasurement> measurements = IntStream.range(0, systems.length)
              .mapToObj(i ->
                  new DerivativeMeasurement() {
                    private final DoubleUnaryOperator toResistivity = operand -> systemsError[i].getApparent(operand);
                    private final TetrapolarSystem system = systems[i].toExact();

                    @Override
                    public double getDerivativeResistivity() {
                      return toResistivity.applyAsDouble(new NormalizedDerivativeR2ByH(system).value(k, h() / system.getL()));
                    }

                    @Override
                    public double getResistivity() {
                      return toResistivity.applyAsDouble(new NormalizedResistance2Layer(system).applyAsDouble(k, h()));
                    }

                    @Nonnull
                    @Override
                    public InexactTetrapolarSystem getSystem() {
                      return InexactTetrapolarSystem.toInexact(ABS_ERROR_OVERALL_DIM, systemsError)[i];
                    }

                    private double h() {
                      return hToDim * OVERALL_DIM;
                    }

                    @Override
                    public String toString() {
                      return "%s; h = %.3f; %s; %s".formatted(getSystem(), h(), Strings.rho(getResistivity()), Strings.dRhoByH(getDerivativeResistivity()));
                    }
                  })
              .collect(Collectors.toUnmodifiableList());
          return inverse.apply(measurements);
        })
        .peek(solution -> LOGGER.config(
            () -> "s/L = [%.3f; %.3f]; k = %.3f; h/D = %.3f; k = %.3f; h = %.6f"
                .formatted(s1 / L, s2 / L, k, hToDim, solution.k12(), solution.h()))
        )
        .map(solution -> new RelativeMediumLayers() {
          @Override
          public double k12() {
            return Inequality.absolute().applyAsDouble(solution.k12(), k) / REL_ERROR_OVERALL_DIM;
          }

          @Override
          public double h() {
            return Inequality.absolute().applyAsDouble(solution.h() / OVERALL_DIM, hToDim) / REL_ERROR_OVERALL_DIM;
          }
        })
        .peek(errorFactors -> LOGGER.config(
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
}
