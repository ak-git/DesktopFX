package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.resistance.NormalizedResistance2Layer;
import com.ak.rsm.resistance.Resistance2LayerTest;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;

public class Apparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLog(@Nonnull double[] rho,
                           @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double logApparent = log(TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity()) - log(rho[0]);

    double logPredicted = Apparent2Rho.newLog1pApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .applyAsDouble(new Layer2RelativeMedium(Layers.getK12(rho[0], rho[1]), hmm / smm));
    Assert.assertEquals(logApparent, logPredicted, 0.001);

    double logPredicted2 = Apparent2Rho.newLog1pApparent2Rho(new RelativeTetrapolarSystem(smm / lmm))
        .applyAsDouble(new Layer2RelativeMedium(Layers.getK12(rho[0], rho[1]), hmm / lmm));
    Assert.assertEquals(logApparent, logPredicted2, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueNormalized(@Nonnull double[] rho,
                                  @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double apparent = TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity() / rho[0];

    double predicted = Apparent2Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .applyAsDouble(new Layer2RelativeMedium(Layers.getK12(rho[0], rho[1]), hmm / smm));
    Assert.assertEquals(apparent, predicted, 0.001);

    double predicted2 = Apparent2Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(smm / lmm))
        .applyAsDouble(new Layer2RelativeMedium(Layers.getK12(rho[0], rho[1]), hmm / lmm));
    Assert.assertEquals(apparent, predicted2, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLogVSNormalized(@Nonnull double[] rho,
                                       @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double k = Layers.getK12(rho[0], rho[1]);
    double apparent1 = log(TetrapolarResistance.ofMilli(smm, lmm).ofOhms(
        new NormalizedResistance2Layer(system).applyAsDouble(k, Metrics.fromMilli(hmm))
    ).resistivity());
    double apparent2 = Apparent2Rho.newLog1pApparent2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k, hmm / lmm));
    Assert.assertEquals(apparent1, apparent2, 0.001);
  }
}