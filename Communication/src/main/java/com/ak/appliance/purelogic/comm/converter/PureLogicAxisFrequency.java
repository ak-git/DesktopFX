package com.ak.appliance.purelogic.comm.converter;

import javax.annotation.Nonnegative;

public enum PureLogicAxisFrequency {
  F6_0(6.0),
  F2_0(2.0),
  F0_5(0.5);

  private final double value;

  PureLogicAxisFrequency(@Nonnegative double value) {
    this.value = value;
  }

  public double value() {
    return value;
  }
}
