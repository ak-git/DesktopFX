package com.ak.rsm.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.rsm.resistance.TetrapolarResistance;
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
    InexactTetrapolarSystem ts1 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 20.0));
    InexactTetrapolarSystem ts2 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(20.0, 10.0));
    InexactTetrapolarSystem ts3 = new InexactTetrapolarSystem(0.2, new TetrapolarSystem(20.0, 30.0));
    return new Object[][] {
        {ts1, ts1, true},
        {ts1, ts2, true},
        {ts1, ts3, false},
        {ts1, new Object(), false},
        {new Object(), ts1, false},
    };
  }

  @Test(dataProvider = "tetrapolar-systems")
  @ParametersAreNonnullByDefault
  public void testEquals(Object system1, Object system2, boolean equals) {
    Assert.assertEquals(system1.equals(system2), equals, "%s compared with %s".formatted(system1, system2));
    Assert.assertEquals(system1.hashCode() == system2.hashCode(), equals, "%s compared with %s".formatted(system1, system2));
    Assert.assertNotEquals(system1, null);
  }

  @DataProvider(name = "inexact-tetrapolar-systems")
  public static Object[][] inexactTetrapolarSystems() {
    return new Object[][] {
        {new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0))},
        {new InexactTetrapolarSystem(0.1, new TetrapolarSystem(30.0, 10.0))},
    };
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testToString(@Nonnull Object system) {
    Assert.assertTrue(system.toString().contains("%.1f".formatted(Metrics.toMilli(0.1))), system.toString());
  }

  @Test
  public void testToString() {
    TetrapolarSystem system = new TetrapolarSystem(10.0, 30.0);
    InexactTetrapolarSystem s = new InexactTetrapolarSystem(0.0, system);
    Assert.assertEquals(s.toString(), system.toString(), s.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testAbsError(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.absError(), 0.1, 0.01, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetDeltaApparent(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.getApparentRelativeError(), 6.0 * 0.1 / 30.0, 1.0e-6, system.toString());
  }

  @Test(dataProvider = "inexact-tetrapolar-systems")
  public void testGetHMax(@Nonnull InexactTetrapolarSystem system) {
    Assert.assertEquals(system.getHMax(1.0), 0.177 * 30.0 / StrictMath.pow(0.1 / 30.0, 1.0 / 3.0), 1.0e-1, system.toString());
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
    InexactTetrapolarSystem system = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0));
    DoubleUnaryOperator rhoAtHMax = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho1;
    PointValuePair optimize = Simplex.optimize(hToL -> {
          double rhoApparent = TetrapolarResistance
              .of(system.system()).rho1(rho1).rho2(rho2).h(hToL[0] * system.system().lCC()).resistivity();
          return DoubleStream.of(-1.0, 1.0)
              .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMax.applyAsDouble(sign), rhoApparent))
              .min().orElseThrow();
        },
        new SimpleBounds(new double[] {0.0}, new double[] {system.getHMax(1.0) / system.system().lCC()})
    );
    Assert.assertEquals(optimize.getPoint()[0], system.getHMax(Layers.getK12(rho1, rho2)) / system.system().lCC(),
        0.1, system.toString());
  }

  @Test(dataProvider = "rho1rho2")
  public void testHMin(@Nonnegative double rho1, @Nonnegative double rho2) {
    if (Double.isFinite(rho2)) {
      InexactTetrapolarSystem system = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0));
      DoubleUnaryOperator rhoAtHMin = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho2;
      PointValuePair optimize = Simplex.optimize(hToL -> {
            double rhoApparent = TetrapolarResistance
                .of(system.system()).rho1(rho1).rho2(rho2).h(hToL[0] * system.system().lCC()).resistivity();
            return DoubleStream.of(-1.0, 1.0)
                .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMin.applyAsDouble(sign), rhoApparent))
                .min().orElseThrow();
          },
          new SimpleBounds(new double[] {0.0}, new double[] {system.getHMax(1.0) / system.system().lCC()})
      );
      Assert.assertEquals(optimize.getPoint()[0], system.getHMin(Layers.getK12(rho1, rho2)) / system.system().lCC(),
          0.01, system.toString());
    }
  }

  @DataProvider(name = "combinations")
  public static Object[][] combinations() {
    return new Object[][] {
        {
            Arrays.asList(
                new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0)),
                new InexactTetrapolarSystem(0.1, new TetrapolarSystem(50.0, 30.0))
            ),
            2
        },
    };
  }

  @Test(dataProvider = "combinations")
  public void testCombinations(@Nonnull Collection<InexactTetrapolarSystem> systems, @Nonnegative int expected) {
    Collection<List<TetrapolarSystem>> c = InexactTetrapolarSystem.getMeasurementsCombination(systems);
    Assert.assertEquals(c.size(), expected, c.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)));
  }
}