package com.ak.appliance.purelogic.comm.converter;

import javax.annotation.Nonnegative;

public enum PureLogicAxisFrequency {
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
