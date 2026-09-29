package com.ak.appliance.caliper.comm.converter;

import com.ak.comm.converter.Variable;

import java.util.Set;

public enum CaliperVariable implements Variable<CaliperVariable> {
  D;

  public static final int FREQUENCY = 200;

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
