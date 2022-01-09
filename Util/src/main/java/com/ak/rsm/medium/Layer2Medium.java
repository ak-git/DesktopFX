package com.ak.rsm.medium;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.Layers;

public final class Layer2Medium extends AbstractMediumLayers {
  private final double dRho2;

  @ParametersAreNonnullByDefault
  public Layer2Medium(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    super(measurements, kw);
    dRho2 = 2.0 * kw.k12AbsError() / StrictMath.pow(1.0 - kw.k12(), 2.0) +
        (rho1().getAbsError() / Layers.getRho1ToRho2(kw.k12()));
  }

  @Override
  public ValuePair rho2() {
    return ValuePair.Name.RHO_2.of(rho1().getValue() / Layers.getRho1ToRho2(kw().k12()), dRho2);
  }

  @Override
  public ValuePair h1() {
    return ValuePair.Name.H.of(kw().hToL() * baseL(), kw().hToLAbsError() * baseL());
  }

  @Override
  public String toString() {
    return "%s; %s; %s; %s".formatted(rho1(), rho2(), h1(), super.toString());
  }
}

