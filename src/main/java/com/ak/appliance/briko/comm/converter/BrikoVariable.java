package com.ak.appliance.briko.comm.converter;

import com.ak.comm.converter.Variable;

import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  C1 {
    @Override
    public Set<Option> options() {
      return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  C2,
  C3,
  C4,
  C5,
  C6,
  C7,
  C8;

  public static final int FREQUENCY = 1000;
}
