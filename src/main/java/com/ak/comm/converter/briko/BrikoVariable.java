package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;

import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A,
  B,
  C,
  D,
  E,
  F;

  @Override
  public Set<Option> options() {
    return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
