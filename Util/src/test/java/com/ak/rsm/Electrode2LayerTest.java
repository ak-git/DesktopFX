package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.ValuePair;
import com.ak.util.CSVLineFileBuilder;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.InexactTetrapolarSystem.systems2;

public class Electrode2LayerTest {
  private static final double OVERALL_DIM = 1.0;
  private static final double REL_ERROR_OVERALL_DIM = 1.0e-6;
  private static final double ABS_ERROR_OVERALL_DIM = REL_ERROR_OVERALL_DIM * OVERALL_DIM;
  private static final ToDoubleFunction<RelativeMediumLayers<ValuePair>> K =
      value -> Math.abs(value.k12().getAbsError() / value.k12().getValue()) / REL_ERROR_OVERALL_DIM;
  private static final ToDoubleFunction<RelativeMediumLayers<ValuePair>> H =
      value -> value.h().getAbsError() / OVERALL_DIM / REL_ERROR_OVERALL_DIM;

  @Test(enabled = false)
  public void test() {
    CSVLineFileBuilder.of((hToDim, k) -> errorsScaleDynamic(new double[] {10.0 / 30.0, 50.0 / 30.0}, k, hToDim))
        .xRange(0.1, 1.0, 0.1)
        .yRange(-1.0, 1.0, 0.1)
        .saveTo("h1", H::applyAsDouble)
        .saveTo("k1", K::applyAsDouble)
        .generate();

    CSVLineFileBuilder.of((hToDim, k) -> errorsScaleStatic(new double[] {10.0 / 30.0, 50.0 / 30.0}, k, hToDim))
        .xRange(0.1, 1.0, 0.1)
        .yRange(-1.0, 1.0, 0.1)
        .saveTo("h2", H::applyAsDouble)
        .saveTo("k2", K::applyAsDouble)
        .generate();
  }

  @DataProvider(name = "single")
  public static Object[][] single() {
    return new Object[][] {
        {
            0.0,
            systems2(0.1, 20.0)
        },
        {
            Layers.getK12(1.0, 4.0),
            systems2(0.01, 10.0)
        },
        {
            Layers.getK12(4.0, 1.0),
            systems2(0.01, 10.0)
        },
        {
            Layers.getK12(1.0, 4.0),
            new InexactTetrapolarSystem[] {
                InexactTetrapolarSystem.milli(0.001).s(10.0).l(50.0),
                InexactTetrapolarSystem.milli(0.001).s(30.0).l(50.0),
            }
        },
        {
            Layers.getK12(1.0, 4.0),
            new InexactTetrapolarSystem[] {
                InexactTetrapolarSystem.milli(0.01).s(60.0).l(20.0),
                InexactTetrapolarSystem.milli(0.01).s(100.0).l(20.0),
            }
        },
    };
  }

  @Test(dataProvider = "single")
  public void testSingle(double k, @Nonnull InexactTetrapolarSystem[] systems) {
    double hToDim = 0.5;
    var errorsScale = errorsScaleDynamic(
        Arrays.stream(systems).map(InexactTetrapolarSystem::toExact).mapToDouble(s -> s.getS() / s.getL()).toArray(),
        k, hToDim);

    double absError = Arrays.stream(systems).mapToDouble(InexactTetrapolarSystem::getAbsError).max().orElseThrow();
    double dim = Arrays.stream(systems).map(InexactTetrapolarSystem::toExact).mapToDouble(s -> Math.max(s.getS(), s.getL())).max().orElseThrow();
    double h = dim * hToDim;
    double dh = -0.0001 * dim;

    DoubleFunction<double[]> rOhms = hx -> Arrays.stream(systems)
        .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 1.0 / Layers.getRho1ToRho2(k), hx))
        .toArray();

    var medium = Inverse.inverseDynamic(
        TetrapolarDerivativeMeasurement.of(systems, rOhms.apply(h), rOhms.apply(h + dh), dh)
    );

    if (Double.compare(k, 0.0) == 0) {
      Assert.assertEquals(K.applyAsDouble(errorsScale),
          (medium.k12().getAbsError() / medium.k12().getValue()) / (absError / dim),
          0.01,
          "%n%s%n%s".formatted(errorsScale, medium));
    }
    else {
      Assert.assertEquals(
          Inequality.proportional().applyAsDouble(
              Math.abs((medium.k12().getAbsError() / medium.k12().getValue()) / (absError / dim)),
              K.applyAsDouble(errorsScale)
          ), 0.1, 0.1,
          "%n%s%n%s".formatted(errorsScale, medium));

      Assert.assertEquals(
          Inequality.proportional().applyAsDouble(medium.h().getAbsError() / absError,
              H.applyAsDouble(errorsScale)), 0.1, 0.1,
          "%n%s%n%s".formatted(errorsScale, medium));
    }
  }

  @Nonnull
  private static RelativeMediumLayers<ValuePair> errorsScaleStatic(@Nonnull double[] sToL, double k, @Nonnegative double hToDim) {
    return errorsScale(sToL, k, hToDim, m -> Inverse.inverseStaticRelative(m, UnaryOperator.identity()));
  }

  @Nonnull
  private static RelativeMediumLayers<ValuePair> errorsScaleDynamic(@Nonnull double[] sToL, double k, @Nonnegative double hToDim) {
    return errorsScale(sToL, k, hToDim, dm -> Inverse.inverseDynamicRelative(dm, initialRelative(sToL, k, hToDim)));
  }

  @Nonnull
  private static RelativeMediumLayers<Double> initialRelative(@Nonnull double[] sToL, double k, @Nonnegative double hToDim) {
    return new RelativeMediumLayers<>() {
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
    };
  }

  private static RelativeMediumLayers<ValuePair> errorsScale(@Nonnull double[] sToL, double k, @Nonnegative double hToDim,
                                                             @Nonnull Function<Collection<DerivativeMeasurement>, RelativeMediumLayers<Double>> inverse) {
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
                return new DerivativeApparent2Rho(system.toRelative()).value(k, h() / system.getL());
              }

              @Override
              public double getResistivity() {
                return new NormalizedApparent2Rho(system.toRelative()).value(k, h() / system.getL());
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
          return new Layer2Medium.DoubleLayer2MediumBuilder(
              ms.stream().map(m -> TetrapolarDerivativePrediction.of(m, kh, rho1)).collect(Collectors.toUnmodifiableList()))
              .layer1(rho1, kh.h()).k12(kh.k12()).build();
        };

    return Inverse.getPairLayer2Medium(measurements, layer2MediumFunction);
  }
}
