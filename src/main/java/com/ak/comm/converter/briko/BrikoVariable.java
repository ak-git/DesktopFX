package com.ak.comm.converter.briko;

import java.util.Collections;
import java.util.Set;

import com.ak.comm.converter.Variable;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A,
  B,
  C {
    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  D,
  E,
  F;


  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
