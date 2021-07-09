package com.ak.rsm;


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;

final class Layer2RelativeMedium implements RelativeMediumLayers {
  @Nonnull
  private final ValuePair k12;
  @Nonnull
  private final ValuePair hToL;

  @ParametersAreNonnullByDefault
  Layer2RelativeMedium(ValuePair k12, ValuePair hToL) {
    this.k12 = k12;
    this.hToL = hToL;
  }

  Layer2RelativeMedium(double k12, @Nonnegative double hToL) {
    this.k12 = ValuePair.Name.K12.of(k12, 0.0);
    this.hToL = ValuePair.Name.H_L.of(hToL, 0.0);
  }

  Layer2RelativeMedium(@Nonnull double[] kw) {
    this(kw[0], kw[1]);
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(k12, hToL);
  }

  @Override
  public double k12() {
    return k12.getValue();
  }

  @Override
  public double hToL() {
    return hToL.getValue();
  }

  @Override
  public double k12AbsError() {
    return k12.getAbsError();
  }

  @Override
  public double hToLAbsError() {
    return hToL.getAbsError();
  }
}
