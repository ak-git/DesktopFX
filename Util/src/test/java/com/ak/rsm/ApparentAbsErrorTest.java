package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApparentAbsErrorTest {

  @Test
  public void testApplyAsDoubleSL() {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(10.0).l(30.0);
    double ohmsR = new Resistance2Layer(system).value(1.0, 4.0, Metrics.fromMilli(5.0));
    Assert.assertEquals(new ApparentAbsError(new TetrapolarMeasurement(system, ohmsR)).getAsDouble(), 0.036, 0.001);
  }

  @Test
  public void testApplyAsDoubleLS() {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(30.0).l(10.0);
    double ohmsR = new Resistance2Layer(system).value(1.0, 4.0, Metrics.fromMilli(5.0));
    Assert.assertEquals(new ApparentAbsError(new TetrapolarMeasurement(system, ohmsR)).getAsDouble(), 0.036, 0.001);
  }
}