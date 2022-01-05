package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InverseDynamicTest {
  private static final Logger LOGGER = Logger.getLogger(InverseDynamicTest.class.getName());

  @DataProvider(name = "relativeDynamicLayer2")
  public static Object[][] relativeDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.000001;
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(rho1).rho2(rho2).h(hmm),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k, 0.00011),
                ValuePair.Name.H_L.of(0.25, 0.000035)
            )
        },
        {
            TetrapolarDerivativeMeasurement.milli(-absErrorMilli).dh(dhMilli).withShiftError().system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm + dhMilli).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k + 0.00011, 0.00011),
                ValuePair.Name.H_L.of(0.25 + 0.000035, 0.000035)
            )
        },
    };
  }

  @Test(dataProvider = "relativeDynamicLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeDynamicLayer2Theory(Collection<? extends DerivativeMeasurement> measurements, RelativeMediumLayers expected) {
    var medium = InverseDynamic.INSTANCE.inverseRelative(measurements);
    Assert.assertEquals(medium.k12(), expected.k12(), expected.k12AbsError(), medium.toString());
    Assert.assertEquals(medium.k12AbsError(), expected.k12AbsError(), expected.k12AbsError() * 0.1, medium.toString());
    Assert.assertEquals(medium.hToL(), expected.hToL(), expected.hToLAbsError(), medium.toString());
    Assert.assertEquals(medium.hToLAbsError(), expected.hToLAbsError(), expected.hToLAbsError() * 0.1, medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "absoluteDynamicLayer2")
  public static Object[][] absoluteDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.000001;
    double hmm = 15.0 / 2;
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00011),
                ValuePair.Name.RHO_2.of(4.0, 0.0018),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.0011))
            }
        },
    };
  }

  @Test(dataProvider = "absoluteDynamicLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseAbsoluteDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, ValuePair[] expected) {
    var medium = InverseDynamic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho1(), expected[0], medium.toString());
    Assert.assertEquals(medium.rho2(), expected[1], medium.toString());
    Assert.assertEquals(medium.h1(), expected[2], medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "theoryDynamicParameters2")
  public static Object[][] theoryDynamicParameters2() {
    double dhMilli = -0.001;
    double hmm = 5.0;
    return new Object[][] {
        {
            List.of(
                TetrapolarDerivativeMeasurement.si(Metrics.fromMilli(0.1)).dh(Metrics.fromMilli(dhMilli))
                    .system(0.01, 0.02).rho1(1.0).rho2(9.0).h(Metrics.fromMilli(hmm))
            ),
            new double[] {
                Apparent2Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new Layer2RelativeMedium(0.8, hmm / 20.0)),
                Apparent2Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new Layer2RelativeMedium(0.8, hmm / 20.0)),
                Double.NaN}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(10.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm / 10.0)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm + dhMilli).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(4.0).rho2(1.0).h(hmm),
            new double[] {4.0, 1.0, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new double[] {1.0, 4.0, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.01).dh(dhMilli).system2(10.0)
                .ofOhms(100.0, 150.0, 90.0, 160.0),
            new double[] {Double.NaN, Double.NaN, Double.NaN}
        },
    };
  }

  @Test(dataProvider = "theoryDynamicParameters2")
  @ParametersAreNonnullByDefault
  public void testInverseDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, double[] expected) {
    var medium = InverseDynamic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho1().getValue(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.rho2().getValue() > 1000 ? Double.POSITIVE_INFINITY : medium.rho2().getValue(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(Metrics.toMilli(medium.h1().getValue()), Metrics.toMilli(expected[2]), 0.01, medium.toString());
    LOGGER.info(medium::toString);
  }
}
