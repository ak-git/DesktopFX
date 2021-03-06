package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NormalizedDerivativeR2ByHTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    TrivariateFunction resistance2Layer = new Resistance2Layer(system);
    double expected = system.getApparent(
        (resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh
    );
    expected /= rho[0];
    double actual = system.getApparent(new NormalizedDerivativeR2ByH(system).value(Layers.getK12(rho[0], rho[1]), hmm / lmm));
    Assert.assertEquals(actual, expected, 0.01);
  }
}