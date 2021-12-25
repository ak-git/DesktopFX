package com.ak.rsm.medium;


import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;

public final class Layer2RelativeMedium implements RelativeMediumLayers {
  @Nonnull
  private final ValuePair k12;
  @Nonnull
  private final ValuePair hToL;

  @ParametersAreNonnullByDefault
  public Layer2RelativeMedium(ValuePair k12, ValuePair hToL) {
    this.k12 = k12;
    this.hToL = hToL;
  }

  public Layer2RelativeMedium(double k12, @Nonnegative double hToL) {
    this.k12 = ValuePair.Name.K12.of(k12, 0.0);
    this.hToL = ValuePair.Name.H_L.of(hToL, 0.0);
  }

  public Layer2RelativeMedium(@Nonnull double[] kw) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    Layer2RelativeMedium that = (Layer2RelativeMedium) o;
    return k12.equals(that.k12) && hToL.equals(that.hToL);
  }

  @Override
  public int hashCode() {
    return Objects.hash(k12, hToL);
  }
}
