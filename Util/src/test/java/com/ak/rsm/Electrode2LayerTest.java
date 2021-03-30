package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.LineFileBuilder;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Electrode2LayerTest {
  private static final Logger LOGGER = Logger.getLogger(Electrode2LayerTest.class.getName());
  private static final double OVERALL_DIM = 1.0;
  private static final double REL_ERROR_OVERALL_DIM = 1.0e-5;
  private static final double ABS_ERROR_OVERALL_DIM = REL_ERROR_OVERALL_DIM * OVERALL_DIM;
  private static final ToDoubleFunction<ValuePair> ERR = value -> value.getAbsError() / REL_ERROR_OVERALL_DIM;
  private static final ToDoubleFunction<RelativeMediumLayers<ValuePair>> K = value -> ERR.applyAsDouble(value.k12());
  private static final ToDoubleFunction<RelativeMediumLayers<ValuePair>> H = value -> ERR.applyAsDouble(value.h());

  @Test(enabled = false)
  public void test() {
    LineFileBuilder.<RelativeMediumLayers<ValuePair>>of("%.3f %.3f %.6f")
        .xStream(() -> DoubleStream.of(0.5))
        .yRange(-1.0, 1.0, 0.1)
        .add("k1.txt", K)
        .add("h1.txt", H)
        .generate((hToL, k) -> errorsScale(new double[] {0.2, 0.6}, k, hToL));

    LineFileBuilder.<RelativeMediumLayers<ValuePair>>of("%.3f %.3f %.6f")
        .xStream(() -> DoubleStream.of(0.5))
        .yRange(-1.0, 1.0, 0.1)
        .add("k2.txt", K)
        .add("h2.txt", H)
        .generate((hToL, k) -> errorsScale(new double[] {0.2, 0.6}, k, hToL,
            derivativeMeasurements -> Inverse.inverseStaticRelative(derivativeMeasurements, UnaryOperator.identity()))
        );
  }

  @Test
  public void testSingle() {
    var errorsScale = errorsScale(new double[] {10.0 / 30.0, 50.0 / 30.0}, Layers.getK12(1.0, 4.0), 10.0 / 50.0);
    Assert.assertEquals(K.applyAsDouble(errorsScale), 10.0, 0.1, errorsScale.toString());
    Assert.assertEquals(H.applyAsDouble(errorsScale), 1.9, 0.1, errorsScale.toString());

    errorsScale = errorsScale(new double[] {10.0 / 30.0, 30.0 / 50.0}, Layers.getK12(1.0, 4.0), 10.0 / 50.0);
    Assert.assertEquals(K.applyAsDouble(errorsScale), 5.7, 0.1, errorsScale.toString());
    Assert.assertEquals(H.applyAsDouble(errorsScale), 1.6, 0.1, errorsScale.toString());
  }

  @Test(enabled = false)
  public void testByH() {
    DoubleStream.iterate(0.2, h -> h < 1.0, h -> h += 0.2)
        .forEach(hToDim -> LOGGER.info(() -> errorsScale(new double[] {1.0 / 3.0, 5.0 / 3.0}, 1.0, hToDim).toString()));
  }

  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers<ValuePair> errorsScale(double[] sToL, double k, @Nonnegative double hToDim) {
    return errorsScale(sToL, k, hToDim,
        derivativeMeasurements -> Inverse.inverseDynamicRelative(derivativeMeasurements,
            new RelativeMediumLayers<>() {
              @Override
              public Double k12() {
                return k;
              }

              /**
               * @return h / L
               */
              @Override
              public Double h() {
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
  private static RelativeMediumLayers<ValuePair> errorsScale(double[] sToL, double k, @Nonnegative double hToDim,
                                                             Function<Collection<DerivativeMeasurement>, RelativeMediumLayers<Double>> inverse) {
    double maxRelDim = Arrays.stream(sToL).reduce(1.0, Math::max);
    DoubleUnaryOperator converterToAbs = rel -> OVERALL_DIM * rel / maxRelDim;
    double s1 = converterToAbs.applyAsDouble(sToL[0]);
    double s2 = converterToAbs.applyAsDouble(sToL[1]);
    double L = converterToAbs.applyAsDouble(1.0);

    InexactTetrapolarSystem[] systems = {
        InexactTetrapolarSystem.si(ABS_ERROR_OVERALL_DIM).s(s1).l(L),
        InexactTetrapolarSystem.si(ABS_ERROR_OVERALL_DIM).s(s2).l(L),
    };

    Collection<DerivativeMeasurement> measurements = Arrays.stream(systems)
        .map(
            s -> new DerivativeMeasurement() {
              private final TetrapolarSystem system = getSystem().toExact();

              @Override
              public double getDerivativeResistivity() {
                return system.getApparent(new NormalizedDerivativeR2ByH(system).value(k, h() / system.getL()));
              }

              @Override
              public double getResistivity() {
                return system.getApparent(new NormalizedResistance2Layer(system).applyAsDouble(k, h()));
              }

              @Nonnull
              @Override
              public InexactTetrapolarSystem getSystem() {
                return s;
              }

              private double h() {
                return hToDim * OVERALL_DIM;
              }

              @Override
              public String toString() {
                return "%s; h = %.3f; %s; %s"
                    .formatted(getSystem(), h(), Strings.rho(getResistivity()), Strings.dRhoByH(getDerivativeResistivity()));
              }
            }
        )
        .collect(Collectors.toUnmodifiableList());

    Function<Collection<DerivativeMeasurement>, Layer2Medium<Double>> layer2MediumFunction =
        ms -> {
          RelativeMediumLayers<Double> kh = inverse.apply(ms);
          double rho1 = 1.0;
          double rho2 = rho1 / Layers.getRho1ToRho2(kh.k12());
          return new Layer2Medium.DoubleLayer2MediumBuilder(
              ms.stream().map(m -> TetrapolarDerivativePrediction.of(m, kh, rho1)).collect(Collectors.toUnmodifiableList()))
              .layer1(rho1, kh.h()).layer2(rho2).build();
        };

    return Inverse.getPairLayer2Medium(measurements, layer2MediumFunction, DerivativeMeasurement::newInstance);
  }
}
