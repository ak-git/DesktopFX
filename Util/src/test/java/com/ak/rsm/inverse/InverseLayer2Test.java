package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InverseLayer2Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer2Test.class.getName());

  @DataProvider(name = "layer2")
  public static Object[][] layer2() {
    double absErrorMilli = 0.001;
    double hmm = 15.0 / 2.0;
    return new Object[][] {
        {
            TetrapolarMeasurement.milli4(absErrorMilli, 10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00047),
                ValuePair.Name.RHO_2.of(4.0, 0.0042),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.009999))
            }
        },
        {
            TetrapolarMeasurement.milli4(absErrorMilli, 10.0).rho1(4.0).rho2(1.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(4.0, 0.0012),
                ValuePair.Name.RHO_2.of(1.0, 0.00049),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.0041))
            }
        },
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(Collection<? extends Measurement> measurements, ValuePair[] expected) {
    var medium = InverseStatic.INSTANCE.inverse(measurements);
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
            TetrapolarMeasurement.milli2(absErrorMilli, 10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {23.1, 4.9}
        },
        {
            TetrapolarMeasurement.milli2Err(-absErrorMilli, 10.0).ofOhms(
                TetrapolarResistance.milli2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            new double[] {23.5, 4.9}
        },
    };
  }

  @Test(dataProvider = "relativeStaticLayer2RiseErrors")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeStaticLayer2RiseErrors(Collection<? extends Measurement> measurements, double[] riseErrors) {
    double absError = measurements.stream().mapToDouble(m -> m.inexact().absError()).average().orElseThrow();
    double L = Measurements.getBaseL(measurements);
    double dim = measurements.stream().mapToDouble(m -> m.inexact().system().getDim()).max().orElseThrow();

    var medium = InverseStatic.INSTANCE.inverseRelative(measurements);
    Assert.assertEquals(medium.k12AbsError() / (absError / dim), riseErrors[0], 0.1, medium.toString());
    Assert.assertEquals(medium.hToLAbsError() / (absError / L), riseErrors[1], 0.1, medium.toString());
    Assert.assertEquals(medium, InverseStatic.INSTANCE.errors(measurements.stream().map(Measurement::inexact).toList(), medium), medium.toString());
    LOGGER.info(medium::toString);
  }
}
