package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnegative;

import com.ak.math.Simplex;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RelativeTetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    RelativeTetrapolarSystem ts = new RelativeTetrapolarSystem(2.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, new RelativeTetrapolarSystem(1.0 / 2.0), true},
        {new RelativeTetrapolarSystem(1.0 / 2.0), new RelativeTetrapolarSystem(1.0 / 3.0), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public void testEquals(RelativeTetrapolarSystem system1, RelativeTetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(1.0 / 2.0);
    Assert.assertNotEquals(system, new Object());
    Assert.assertNotEquals(new Object(), system);
  }

  @DataProvider(name = "relative-tetrapolar-systems")
  public static Object[][] relativeTetrapolarSystems() {
    return new Object[][] {
        {2.0},
        {0.5},
        {1.0 / 3.0},
        {3.0}
    };
  }

  @Test(dataProvider = "relative-tetrapolar-systems")
  public void testToString(@Nonnegative double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    Assert.assertEquals(system.toString(), "s / L = %.3f".formatted(sToL), system.toString());
  }

  @Test(dataProvider = "relative-tetrapolar-systems")
  public void testHash(@Nonnegative double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    Assert.assertEquals(system.hashCode(), Double.hashCode(Math.min(sToL, 1.0 / sToL)), system.toString());
  }

  @Test(dataProvider = "relative-tetrapolar-systems")
  public void testErrorFactor(@Nonnegative double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    Assert.assertEquals(system.errorFactor(), 6.0, 0.1);
  }

  @Test
  public void testHMaxFactor() {
    PointValuePair pair = Simplex.optimize(x -> 1.0 - new RelativeTetrapolarSystem(x[0]).hMaxFactor(1.0),
        new SimpleBounds(new double[] {0.0}, new double[] {1.0}), new double[] {0.5}, new double[] {0.01}
    );
    Assert.assertEquals(pair.getPoint()[0], 1.0 / 3.0, 0.001, Arrays.toString(pair.getPoint()));
  }

  @Test
  public void testHMinFactor() {
    PointValuePair pair = Simplex.optimize(ks -> new RelativeTetrapolarSystem(ks[1]).hMinFactor(ks[0]),
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {0.0, 0.9}), new double[] {-0.5, 0.1}, new double[] {0.01, 0.1}
    );
    Assert.assertEquals(pair.getPoint()[0], -1.0, 0.001, Arrays.toString(pair.getPoint()));
  }
}