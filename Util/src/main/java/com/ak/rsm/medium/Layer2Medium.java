package com.ak.rsm.medium;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.Layers;

final class Layer2Medium extends AbstractMediumLayers {
  @Nonnull
  private final ValuePair rho2;
  @Nonnull
  private final ValuePair h1;

  @ParametersAreNonnullByDefault
  Layer2Medium(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    super(measurements, kw);

    var dRho2 = 2.0 * kw.k12AbsError() / StrictMath.pow(1.0 - kw.k12(), 2.0);
    dRho2 += rho1().getAbsError() / Layers.getRho1ToRho2(kw.k12());

    rho2 = ValuePair.Name.RHO_2.of(rho1().getValue() / Layers.getRho1ToRho2(kw.k12()), dRho2);
    double baseL = Measurements.getBaseL(measurements);
    h1 = ValuePair.Name.H.of(kw.hToL() * baseL, kw.hToLAbsError() * baseL);
  }

  @Override
  public ValuePair rho2() {
    return rho2;
  }

  @Override
  public ValuePair h1() {
    return h1;
  }

  @Override
  public String toString() {
    return "%s; %s; %s; %s".formatted(rho1(), rho2, h1, super.toString());
  }
}

