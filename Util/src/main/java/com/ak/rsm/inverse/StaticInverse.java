package com.ak.rsm.inverse;

import com.ak.rsm.resistance.Resistivity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

final class StaticInverse extends AbstractInverseFunction<Resistivity> {
  @ParametersAreNonnullByDefault
  StaticInverse(Collection<? extends Resistivity> r) {
    super(r, Resistivity::resistivity, Layer2StaticInverse::new);
  }
}
