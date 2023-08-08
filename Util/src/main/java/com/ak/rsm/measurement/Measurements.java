package com.ak.rsm.measurement;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer1RelativeMedium;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

public enum Measurements {
  ;

  @Nonnull
  @ParametersAreNonnullByDefault
  public static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (Layer1RelativeMedium.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.resistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.inexact().getApparentRelativeError());
    }
    else if (Layer1RelativeMedium.NAN.equals(kw)) {
      return ValuePair.Name.RHO_1.of(Double.NaN, Double.NaN);
    }
    else {
      double baseL = Resistivity.getBaseL(measurements);
      return measurements.stream()
          .<ValuePair>mapMulti((measurement, consumer) -> {
            TetrapolarSystem s = measurement.system();
            RelativeMediumLayers layer2RelativeMedium = new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / s.lCC());

            double normApparent = Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium);
            double fK = Math.abs(Apparent2Rho.newDerApparentByKDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerApparentByPhiDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.hToLAbsError());
            double rho1 = measurement.resistivity() / normApparent;
            consumer.accept(ValuePair.Name.RHO_1.of(rho1, ((fK + fPhi) / normApparent) * rho1));

            if (measurement instanceof TetrapolarDerivativeMeasurement dm && !Double.isNaN(dm.derivativeResistivity())) {
              double normDer = Apparent2Rho.newDerApparentByPhiDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium);
              double fKDer = Math.abs(Apparent2Rho.newSecondDerApparentByPhiKDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.k12AbsError());
              double fPhiDer = Math.abs(Apparent2Rho.newSecondDerApparentByPhiPhiDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.hToLAbsError());
              double rho1Der = dm.derivativeResistivity() / normDer;
              consumer.accept(ValuePair.Name.RHO_1.of(rho1Der, ((fKDer + fPhiDer) / normDer) * rho1Der));
            }
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }
}

