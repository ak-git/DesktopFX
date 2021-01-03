package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import org.testng.annotations.Test;

public class Electrode2LayerTest {
  @Test(enabled = false)
  public void test() {
    LineFileBuilder.<RelativeMediumLayers>of("%.3f %.3f %.6f")
        .xStream(() -> DoubleStream.of(0.5))
        .yRange(-1.0, 1.0, 0.1)
        .add("k1.txt", RelativeMediumLayers::k12)
        .add("h1.txt", RelativeMediumLayers::h)
        .generate((hToL, k) -> errorsScale(new double[] {0.2, 0.6}, k, hToL,
            derivativeMeasurements -> Inverse.inverseDynamicRelative(derivativeMeasurements,
                new RelativeMediumLayers() {
                  @Override
                  public double k12() {
                    return k;
                  }

                  @Override
                  public double h() {
                    return hToL;
                  }
                })
            )
        );

    LineFileBuilder.<RelativeMediumLayers>of("%.3f %.3f %.6f")
        .xStream(() -> DoubleStream.of(0.5))
        .yRange(-1.0, 1.0, 0.1)
        .add("k2.txt", RelativeMediumLayers::k12)
        .add("h2.txt", RelativeMediumLayers::h)
        .generate((hToL, k) -> errorsScale(new double[] {0.2, 0.6}, k, hToL,
            derivativeMeasurements -> Inverse.inverseStaticRelative(derivativeMeasurements, UnaryOperator.identity()))
        );
  }

  private static RelativeMediumLayers errorsScale(@Nonnull double[] sToL, double k, @Nonnegative double hToL,
                                                  @Nonnull Function<Collection<DerivativeMeasurement>, RelativeMediumLayers> inverse) {
    final double L = 1.0;
    final double absErrorL = 1.0E-6 * L;
    TetrapolarSystem[] systems = {
        TetrapolarSystem.milli().s(L * sToL[0]).l(L * sToL[1]),
        TetrapolarSystem.milli().s(L * sToL[1]).l(L),
    };

    return IntStream.of(2, 5)
        .mapToObj(n -> {
          int signS1 = (n & 1) == 0 ? 1 : -1;
          int signL = (n & 2) == 0 ? 1 : -1;
          int signS2 = (n & 4) == 0 ? 1 : -1;

          TetrapolarSystem[] systemsError = {
              systems[0].newWithError(absErrorL, signS1, signL),
              systems[1].newWithError(absErrorL, signL, signS2)
          };

          Collection<DerivativeMeasurement> measurements = IntStream.range(0, systems.length)
              .mapToObj(i ->
                  new DerivativeMeasurement() {
                    @Override
                    public double getDerivativeResistivity() {
                      return getSystem().getApparent(new NormalizedDerivativeR2ByH(systemsError[i]).value(k, hToL));
                    }

                    @Override
                    public double getResistivity() {
                      return getSystem().getApparent(new NormalizedResistance2Layer(systemsError[i]).applyAsDouble(k, hToL));
                    }

                    @Nonnull
                    @Override
                    public TetrapolarSystem getSystem() {
                      return systems[i];
                    }
                  })
              .collect(Collectors.toUnmodifiableList());

          RelativeMediumLayers relativeMediumLayers = inverse.apply(measurements);

          return new RelativeMediumLayers() {
            private static final double REL_ERROR = absErrorL / L;

            @Override
            public double k12() {
              return Inequality.proportional().applyAsDouble(relativeMediumLayers.k12(), k) / REL_ERROR;
            }

            @Override
            public double h() {
              return Inequality.absolute().applyAsDouble(relativeMediumLayers.h(), hToL) / L / REL_ERROR;
            }

            @Override
            public String toString() {
              return "%d [%+d %+d %+d]\th / L = %.4f; eK = %.6f; eH = %.6f; [%s]"
                  .formatted(n, signS1, signL, signS2, hToL, k12(), h(), Arrays.toString(sToL));
            }
          };
        })
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
                    return "h / L = %.4f; eK = %.6f; eH = %.6f; [%s]".formatted(hToL, k12(), h(), Arrays.toString(sToL));
                  }
                }
            )
        );
  }
}
