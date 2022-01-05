package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.Layers;
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
    };
  }

  @Test(dataProvider = "relativeDynamicLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeDynamicLayer2Theory(Collection<? extends DerivativeMeasurement> measurements, RelativeMediumLayers expected) {
    var medium = InverseDynamic.INSTANCE.inverseRelative(measurements);
    LOGGER.info(medium::toString);
    Assert.assertEquals(medium.k12(), expected.k12(), expected.k12AbsError(), medium.toString());
    Assert.assertEquals(medium.k12AbsError(), expected.k12AbsError(), expected.k12AbsError() * 0.1, medium.toString());
    Assert.assertEquals(medium.hToL(), expected.hToL(), expected.hToLAbsError(), medium.toString());
    Assert.assertEquals(medium.hToLAbsError(), expected.hToLAbsError(), expected.hToLAbsError() * 0.1, medium.toString());
  }
}
