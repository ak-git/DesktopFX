package com.ak.digitalfilter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FIRFilterTest {
  @DataProvider(name = "simple")
  public Object[][] simple() {
    return new Object[][] {{
        new double[] {1.0, 2.0, 4.0, 8.0, 5.0, 2.0, 1.0},
        new double[] {-1.0, 0.0, 1.0},
        new double[] {1.0, 2.0, 4.0 - 1.0, 8.0 - 2.0, 5.0 - 4.0, 2.0 - 8.0, 1.0 - 5.0},
        1.0
    }, {
        new double[] {1.0, 2.0, 4.0, 8.0, 5.0, 2.0, 1.0},
        new double[] {1.0, 2.0},
        new double[] {2.0, 5.0, 10.0, 20.0, 18.0, 9.0, 4.0},
        0.5
    },
    };
  }

  @Test(dataProvider = "simple")
  public void testApplyAsDouble(double[] input, double[] koeff, double[] result, double delay) {
    FIRFilter filter = new FIRFilter(koeff);
    for (int i = 0; i < input.length; i++) {
      Assert.assertEquals(filter.applyAsDouble(input[i]), result[i], 1.0e-3, String.format("Step %d of [0 - %d]",
          i, input.length));
    }
    Assert.assertEquals(filter.delay(), delay, 1.0e-3, filter.toString());
  }
}