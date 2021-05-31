package com.ak.rsm;


import com.ak.util.Strings;

record Layer2RelativeMedium<D>(D k12, D hToL) implements RelativeMediumLayers<D> {
  @Override
  public D hToL() {
    return hToL;
  }

  @Override
  public String toString() {
    return "k%s%s = %s; %s = %s".formatted(Strings.low(1), Strings.low(2), k12, Strings.PHI, hToL);
  }
}
