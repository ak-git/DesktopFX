package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class DerivativeR2ByHTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    TrivariateFunction resistance2Layer = new Resistance2Layer(system);
    double expected = (resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh;
    double actual = new DerivativeR2ByH(system).value(rho[0], rho[1], Metrics.fromMilli(hmm));
    Assert.assertEquals(actual, expected, 0.1);
  }
}