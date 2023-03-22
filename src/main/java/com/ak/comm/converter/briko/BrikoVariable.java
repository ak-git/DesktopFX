package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;

import java.util.Collections;
import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  C1 {
    @Override
    public Set<Option> options() {
      return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  C2 {
    @Override
    public Set<Option> options() {
      return C1.options();
    }
  },
  C3,
  C4,
  IGNORE1,
  IGNORE2;

  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
