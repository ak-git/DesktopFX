package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.medium.Layer2RelativeMedium;
import com.ak.rsm.resistance.Resistance2LayerTest;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DerivativeApparentByK2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    if (Double.compare(rho[0], rho[1]) != 0) {
      double k12 = Layers.getK12(rho[0], rho[1]);
      double dk = 0.00001;
      var b = TetrapolarResistance.milli(smm, lmm);
      double expected = (
          b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12)).h(hmm).resistivity() -
              b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12 - dk)).h(hmm).resistivity()
      ) / dk;
      expected /= rho[0];
      double actual = Apparent2Rho.newDerivativeApparentByK2Rho(new RelativeTetrapolarSystem(smm / lmm))
          .applyAsDouble(new Layer2RelativeMedium(k12, hmm / lmm));
      Assert.assertEquals(actual, expected, 0.1);
    }
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    if (Double.compare(rho[0], rho[1]) != 0) {
      double k12 = Layers.getK12(rho[0], rho[1]);
      double dk = 0.00001;
      var b = TetrapolarResistance.milli(lmm, smm);
      double expected = (
          b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12)).h(hmm).resistivity() -
              b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12 - dk)).h(hmm).resistivity()
      ) / dk;
      expected /= rho[0];
      double actual = Apparent2Rho.newDerivativeApparentByK2Rho(new RelativeTetrapolarSystem(lmm / smm))
          .applyAsDouble(new Layer2RelativeMedium(k12, hmm / smm));
      Assert.assertEquals(actual, expected, 0.1);
    }
  }
}