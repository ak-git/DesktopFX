package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InexactTetrapolarSystemTest {
  @DataProvider(name = "tetrapolar-systems")
  public static Object[][] tetrapolarSystems() {
    TetrapolarSystem ts = TetrapolarSystem.si(0.1).s(10.0).l(20.0);
    return new Object[][] {
        {ts, ts, true},
        {ts, TetrapolarSystem.si(0.1).s(20.0).l(10.0), true},
        {ts, TetrapolarSystem.si(0.1).s(20.0).l(30.0), false}
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  public void testEquals(TetrapolarSystem system1, TetrapolarSystem system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(String.valueOf(system1), system2));
    Assert.assertNotEquals(system1, new Object());
    Assert.assertNotEquals(new Object(), system1);
  }

  @Test
  public void testNotEquals() {
    TetrapolarSystem system = TetrapolarSystem.milli(0.01).s(10.0).l(30.0);
    Assert.assertNotEquals(system, new Object());
    Assert.assertNotEquals(new Object(), system);
  }

  @DataProvider(name = "inexact-tetrapolar-systems")
  public static Object[][] inexactTetrapolarSystems() {
    return new Object[][] {
        {TetrapolarSystem.milli(0.1).s(10.0).l(30.0)},
        {TetrapolarSystem.milli(0.1).s(30.0).l(10.0)},
    };
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testToString(@Nonnull TetrapolarSystem system) {
    Assert.assertTrue(system.toString().startsWith(system.toString()), system.toString());
    Assert.assertTrue(system.toString().contains("%.1f".formatted(0.1)), system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testAbsError(@Nonnull TetrapolarSystem system) {
    Assert.assertEquals(system.getAbsError(), Metrics.fromMilli(0.1), 0.01, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetDeltaApparent(@Nonnull TetrapolarSystem system) {
    Assert.assertEquals(system.getApparentRelativeError(), 6.0 * 0.1 / 30.0, 1.0e-6, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetDiffDeltaApparent(@Nonnull TetrapolarSystem system) {
    Assert.assertEquals(system.getDiffApparentRelativeError(), 7.0 * 0.1 / 30.0, 1.0e-6, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetHMax(@Nonnull TetrapolarSystem system) {
    Assert.assertEquals(system.getHMax(1.0), 0.177 * 0.03 / StrictMath.pow(0.1 / 30.0, 1.0 / 3.0), 1.0e-3, system.toString());
  }

  @Test
  public void testShift() {
    TetrapolarSystem initial = TetrapolarSystem.milli(0.1).s(20.0).l(10.0);
    Assert.assertEquals(initial.toRelative().errorFactor(), 6.0, 0.01);
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
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(10.0).l(30.0);
    DoubleUnaryOperator rhoAtHMax = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho1;
    PointValuePair optimize = Simplex.optimizeAll(hToL -> {
          double rhoApparent = system.getApparent(new Resistance2Layer(system).value(rho1, rho2, hToL[0] * system.getL()));
          return DoubleStream.of(-1.0, 1.0)
              .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMax.applyAsDouble(sign), rhoApparent))
              .min().orElseThrow();
        },
        new SimpleBounds(new double[] {0.0}, new double[] {system.getHMax(1.0) / system.getL()}), new double[] {0.01}
    );
    Assert.assertEquals(optimize.getPoint()[0], system.getHMax(Layers.getK12(rho1, rho2)) / system.getL(),
        0.1, system.toString());
  }

  @Test(dataProvider = "rho1rho2")
  public void testHMin(@Nonnegative double rho1, @Nonnegative double rho2) {
    if (Double.isFinite(rho2)) {
      TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(10.0).l(30.0);
      DoubleUnaryOperator rhoAtHMin = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho2;
      PointValuePair optimize = Simplex.optimizeAll(hToL -> {
            double rhoApparent = system.getApparent(new Resistance2Layer(system).value(rho1, rho2, hToL[0] * system.getL()));
            return DoubleStream.of(-1.0, 1.0)
                .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMin.applyAsDouble(sign), rhoApparent))
                .min().orElseThrow();
          },
          new SimpleBounds(new double[] {0.0}, new double[] {system.getHMax(1.0) / system.getL()}), new double[] {0.01}
      );
      Assert.assertEquals(optimize.getPoint()[0], system.getHMin(Layers.getK12(rho1, rho2)) / system.getL(),
          0.01, system.toString());
    }
  }

  @DataProvider(name = "combinations")
  public static Object[][] combinations() {
    return new Object[][] {
        {
            Arrays.asList(TetrapolarSystem.systems2(0.1, 10.0)),
            2
        },
    };
  }

  @Test(dataProvider = "combinations")
  public void testCombinations(@Nonnull Collection<TetrapolarSystem> systems, @Nonnegative int expected) {
    Collection<List<TetrapolarSystem>> c = TetrapolarSystem.getMeasurementsCombination(systems);
    Assert.assertEquals(c.size(), expected, c.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)));
  }
}