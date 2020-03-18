package com.ak.comm.converter.aper;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel2;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class AperTest {
  @Test
  public void testVariableProperties() {
    Assert.assertEquals(AperOutVariable.CCR1.getUnit(), Units.OHM);
    Assert.assertTrue(AperOutVariable.CCR1.options().contains(Variable.Option.TEXT_VALUE_BANNER));

    EnumSet.of(AperOutVariable.R1, AperOutVariable.R2).forEach(variable -> Assert.assertEquals(variable.getUnit(), MetricPrefix.MILLI(Units.OHM)));
    EnumSet.of(AperOutVariable.R1, AperOutVariable.R2).forEach(variable -> Assert.assertTrue(variable.options().contains(Variable.Option.VISIBLE)));
  }

  @Test
  public void testInputVariablesClass() {
    EnumSet.allOf(AperOutVariable.class).forEach(variable -> Assert.assertEquals(variable.getInputVariablesClass(), AperInVariable.class));
  }

  @DataProvider(name = "filter-delay")
  public static Object[][] filterDelay() {
    return new Object[][] {
        {AperOutVariable.R1, 0.0},
        {AperOutVariable.R2, 0.0},
        {AperOutVariable.CCR1, 0.0},
    };
  }

  @Test(dataProvider = "filter-delay")
  public void testFilterDelay(@Nonnull AperOutVariable variable, double delay) {
    Assert.assertEquals(variable.filter().getDelay(), delay, 0.001, variable.toString());
  }

  @Test(enabled = false)
  public void testSplineSurface1() {
    SplineCoefficientsUtils.testSplineSurface1(AperSurfaceCoefficientsChannel1.class);
  }

  @Test(enabled = false)
  public void testSplineSurface2() {
    SplineCoefficientsUtils.testSplineSurface2(AperSurfaceCoefficientsChannel2.class);
  }
}