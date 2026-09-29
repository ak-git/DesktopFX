package com.ak.appliance.caliper.comm.converter;

import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

import static com.ak.appliance.caliper.comm.converter.CaliperVariable.FREQUENCY;

public final class CaliperConverter extends AbstractConverter<String, CaliperVariable> {
  public CaliperConverter() {
    super(CaliperVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(String frame) {
    return Stream.of(new int[] {frame.length()});
  }
}
