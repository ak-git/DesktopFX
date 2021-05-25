package com.ak.rsm;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.util.Strings;

final class Layer2RelativeMedium<D> implements RelativeMediumLayers<D> {
  private final D k12;
  @Nonnull
  private final D hToL;

  @ParametersAreNonnullByDefault
  Layer2RelativeMedium(D k12, D hToL) {
    this.k12 = k12;
    this.hToL = hToL;
  }

  @Override
  public D k12() {
    return k12;
  }

  @Override
  public D hToL() {
    return hToL;
  }

  @Override
  public String toString() {
    return "k%s%s = %s; %s = %s".formatted(Strings.low(1), Strings.low(2), k12, Strings.PHI, hToL);
  }
}
