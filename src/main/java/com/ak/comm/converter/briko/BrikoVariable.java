package com.ak.comm.converter.briko;

import java.util.Collections;
import java.util.Set;

import com.ak.comm.converter.Variable;

public enum BrikoVariable implements Variable<BrikoVariable> {
  C1 {
    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  C2,
  C3,
  C4,
  E,
  F;


  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
