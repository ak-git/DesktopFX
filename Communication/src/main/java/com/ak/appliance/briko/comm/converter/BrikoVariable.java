package com.ak.appliance.briko.comm.converter;

import com.ak.comm.converter.Variable;

import java.util.Collections;
import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A {
    @Override
    public Set<Option> options() {
      return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  B,
  C,
  D,
  E,
  F;

  public static final int FREQUENCY = 1000;

  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
