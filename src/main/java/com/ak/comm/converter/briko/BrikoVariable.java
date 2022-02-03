package com.ak.comm.converter.briko;

import java.util.Collections;
import java.util.Set;

import com.ak.comm.converter.Variable;

public enum BrikoVariable implements Variable<BrikoVariable> {
  AD1,
  AD2,
  HX1 {
    @Override
    public Set<Option> options() {
      return Option.defaultOptions();
    }
  },
  HX2,
  A1,
  A2;

  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
