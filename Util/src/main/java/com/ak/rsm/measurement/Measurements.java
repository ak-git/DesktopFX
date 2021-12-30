package com.ak.rsm.measurement;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.TetrapolarSystem;

import static java.lang.StrictMath.pow;

public enum Measurements {
  ;

  @Nonnegative
  public static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.stream().mapToDouble(m -> m.system().system().lCC()).max().orElseThrow();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.resistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.system().getApparentRelativeError());
    }
    else {
      double baseL = getBaseL(measurements);
      return measurements.stream().parallel()
          .map(measurement -> {
            TetrapolarSystem s = measurement.system().system();
            double normApparent = Apparent2Rho.newNormalizedApparent2Rho(s.relativeSystem())
                .applyAsDouble(new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / s.lCC()));

            double fK = Math.abs(Apparent2Rho.newDerivativeApparentByK2Rho(s.relativeSystem()).applyAsDouble(kw) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem()).applyAsDouble(kw) * kw.hToLAbsError());

            return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent,
                (fK + fPhi) * measurement.resistivity() / pow(normApparent, 2.0)
            );
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }
}

