package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;

import java.util.Collections;
import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A,
  B,
  C {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  D {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  E,
  F {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  };

  @Override
  public Set<Option> options() {
    return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
