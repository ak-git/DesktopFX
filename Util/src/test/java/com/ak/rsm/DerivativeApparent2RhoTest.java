package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class DerivativeApparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.001);
    TrivariateFunction resistance2Layer = new Resistance2Layer(system);
    double expected = new Resistance1Layer(system).getApparent(
        (resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh
    );
    expected /= rho[0];
    double actual = new DerivativeApparent2Rho(system).value(Layers.getK12(rho[0], rho[1]), Metrics.fromMilli(hmm));
    Assert.assertEquals(StrictMath.log(Math.abs(actual)), StrictMath.log(Math.abs(expected)), 0.1);
    Assert.assertEquals(Math.signum(actual), Math.signum(expected), 0.1);
  }
}