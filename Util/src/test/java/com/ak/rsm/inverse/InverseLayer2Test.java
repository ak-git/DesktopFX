package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
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
}
