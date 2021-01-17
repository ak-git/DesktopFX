package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InexactTetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    InexactTetrapolarSystem ts = InexactTetrapolarSystem.si(0.1).s(10.0).l(20.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, InexactTetrapolarSystem.si(0.1).s(20.0).l(10.0), true},
        {ts, InexactTetrapolarSystem.si(0.1).s(20.0).l(30.0), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public void testEquals(InexactTetrapolarSystem system1, InexactTetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.01).s(10.0).l(30.0);
    Assert.assertNotEquals(system, new Object());
    Assert.assertNotEquals(new Object(), system);
  }

  @DataProvider(name = "inexact-tetrapolar-systems")
  public static Object[][] inexactTetrapolarSystems() {
    return new Object[][] {
        {InexactTetrapolarSystem.milli(0.1).s(10.0).l(30.0)},
        {InexactTetrapolarSystem.milli(0.1).s(30.0).l(10.0)},
    };
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testToString(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertTrue(system.toString().startsWith(system.toExact().toString()), system.toString());
    Assert.assertTrue(system.toString().contains("%.1f".formatted(0.1)), system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testAbsError(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.getAbsError(), Metrics.fromMilli(0.1), 0.01, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetDeltaApparent(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.getApparentRelativeError(), 6.0 * 0.1 / 30.0, 1.0e-6, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetHMax(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.getHMax(1.0), 0.177 * 0.03 / StrictMath.pow(0.1 / 30.0, 1.0 / 3.0), 1.0e-3, system.toString());
  }

  @Test
  public void testShift() {
    InexactTetrapolarSystem initial = InexactTetrapolarSystem.milli(0.1).s(20.0).l(10.0);
    Assert.assertEquals(initial.toExact().toRelative().errorFactor(), 6.0, 0.01);
    Assert.assertEquals(initial.shift(1, -1).toRelative().errorFactor(), 5.97, 0.01);
    Assert.assertEquals(initial.shift(-1, -1).toRelative().errorFactor(), 5.99, 0.01);
    Assert.assertEquals(initial.shift(1, 1).toRelative().errorFactor(), 6.01, 0.01);
    Assert.assertEquals(initial.shift(-1, 1).toRelative().errorFactor(), 6.03, 0.01);
  }

  @DataProvider(name = "rho1rho2")
  public static Object[][] rho1rho2() {
    return new Object[][] {
        {1.0, Double.POSITIVE_INFINITY},
        {10.0, Double.POSITIVE_INFINITY},
        {0.1, Double.POSITIVE_INFINITY},
        {1.0, 2.0},
        {2.0, 1.0},
    };
  }

  @Test(dataProvider = "rho1rho2")
  public void testHMax(@Nonnegative double rho1, @Nonnegative double rho2) {
    InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.1).s(10.0).l(30.0);
    DoubleUnaryOperator rhoAtHMax = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho1;
    PointValuePair optimize = Simplex.optimize("", hToL -> {
          double rhoApparent = system.toExact().getApparent(Resistance2Layer.layer2(rho1, rho2, hToL[0] * system.toExact().getL()).applyAsDouble(system));
          return DoubleStream.of(-1.0, 1.0)
              .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMax.applyAsDouble(sign), rhoApparent))
              .min().orElseThrow();
        },
        new SimpleBounds(new double[] {0.0}, new double[] {system.getHMax(1.0) / system.toExact().getL()}),
        new double[] {0.0}, new double[] {0.01}
    );
    Assert.assertEquals(optimize.getPoint()[0], system.getHMax(Layers.getK12(rho1, rho2)) / system.toExact().getL(),
        0.1, system.toString());
  }

  @Test(dataProvider = "rho1rho2")
  public void testHMin(@Nonnegative double rho1, @Nonnegative double rho2) {
    if (Double.isFinite(rho2)) {
      InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.1).s(10.0).l(30.0);
      DoubleUnaryOperator rhoAtHMin = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho2;
      PointValuePair optimize = Simplex.optimize("", hToL -> {
            double rhoApparent = system.toExact().getApparent(Resistance2Layer.layer2(rho1, rho2, hToL[0] * system.toExact().getL()).applyAsDouble(system));
            return DoubleStream.of(-1.0, 1.0)
                .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMin.applyAsDouble(sign), rhoApparent))
                .min().orElseThrow();
          },
          new SimpleBounds(new double[] {0.0}, new double[] {system.getHMax(1.0) / system.toExact().getL()}),
          new double[] {0.0}, new double[] {0.01}
      );
      Assert.assertEquals(optimize.getPoint()[0], system.getHMin(Layers.getK12(rho1, rho2)) / system.toExact().getL(),
          0.01, system.toString());
    }
  }
}