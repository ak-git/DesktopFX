package com.ak.rsm.inverse;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InverseStaticTest {
  private static final Logger LOGGER = Logger.getLogger(InverseStaticTest.class.getName());

  @DataProvider(name = "layer1")
  public static Object[][] layer1() {
    RandomGenerator random = new SecureRandom();
    int rho = random.nextInt(9) + 1;
    return new Object[][] {
        {
            TetrapolarMeasurement.milli(0.1).system2(10.0).rho(rho),
            rho
        },
    };
  }

  @Test(dataProvider = "layer1")
  public void testInverseLayer1(@Nonnull Collection<? extends Measurement> measurements, @Nonnegative double expected) {
    var medium = new StaticAbsolute(measurements).get();
    Assert.assertEquals(medium.rho().getValue(), expected, 0.2, medium.toString());
    measurements.forEach(m ->
        Assert.assertTrue(
            medium.rho().getAbsError() / medium.rho().getValue() < m.inexact().getApparentRelativeError(),
            medium.toString()
        )
    );
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "layer2")
  public static Object[][] layer2() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2.0;
    return new Object[][] {
        {
            TetrapolarMeasurement.milli(absErrorMilli).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.0022),
                ValuePair.Name.RHO_2.of(4.0, 0.015),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.050))
            }
        },
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(Collection<? extends Measurement> measurements, ValuePair[] expected) {
    var medium = new StaticAbsolute(measurements).get();
    Assert.assertEquals(medium.rho1(), expected[0], medium.toString());
    Assert.assertEquals(medium.rho2(), expected[1], medium.toString());
    Assert.assertEquals(medium.h1(), expected[2], medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "relativeStaticLayer2RiseErrors")
  public static Object[][] relativeStaticLayer2RiseErrors() {
    double absErrorMilli = 0.001;
    double hmm = 15.0;
    return new Object[][] {
        {
            TetrapolarMeasurement.milli(absErrorMilli).system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {136.7, 31.0}
        },
        {
            TetrapolarMeasurement.milli(-absErrorMilli).withShiftError().system2(10.0).ofOhms(
                TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            new double[] {138.7, 31.2}
        },
    };
  }

  @Test(dataProvider = "relativeStaticLayer2RiseErrors")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeStaticLayer2RiseErrors(Collection<? extends Measurement> measurements, double[] riseErrors) {
    double absError = measurements.stream().mapToDouble(m -> m.inexact().absError()).average().orElseThrow();
    double L = Measurements.getBaseL(measurements);
    double dim = measurements.stream().mapToDouble(m -> m.system().getDim()).max().orElseThrow();

    var medium = new StaticRelative(measurements).get();
    Assert.assertEquals(medium.k12AbsError() / (absError / dim), riseErrors[0], 0.1, medium.toString());
    Assert.assertEquals(medium.hToLAbsError() / (absError / L), riseErrors[1], 0.1, medium.toString());
    Assert.assertEquals(medium, new StaticAbsolute(measurements).apply(medium), medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "relativeStaticLayer2")
  public static Object[][] relativeStaticLayer2() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Object[][] {
        {
            TetrapolarMeasurement.milli(absErrorMilli).system2(10.0).rho1(1.0).rho2(rho2).h(hmm),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.6, 0.0010),
                ValuePair.Name.H_L.of(0.25, 0.00039)
            )
        },
        {
            TetrapolarMeasurement.milli(-absErrorMilli).withShiftError().system2(10.0).ofOhms(
                TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.599, 0.0010),
                ValuePair.Name.H_L.of(0.2496, 0.00039)
            )
        },
    };
  }

  @Test(dataProvider = "relativeStaticLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeStaticLayer2(Collection<? extends Measurement> measurements, RelativeMediumLayers expected) {
    var medium = new StaticRelative(measurements).get();
    Assert.assertEquals(medium.k12(), expected.k12(), expected.k12AbsError(), medium.toString());
    Assert.assertEquals(medium.k12AbsError(), expected.k12AbsError(), expected.k12AbsError() * 0.1, medium.toString());
    Assert.assertEquals(medium.hToL(), expected.hToL(), expected.hToLAbsError(), medium.toString());
    Assert.assertEquals(medium.hToLAbsError(), expected.hToLAbsError(), expected.hToLAbsError() * 0.1, medium.toString());
    LOGGER.info(medium::toString);
  }
}
