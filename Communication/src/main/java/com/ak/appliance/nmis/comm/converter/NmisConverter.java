package com.ak.appliance.nmis.comm.converter;

import com.ak.appliance.nmis.comm.bytes.NmisResponseFrame;
import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

public final class NmisConverter extends AbstractConverter<NmisResponseFrame, NmisVariable> {
  public NmisConverter() {
    super(NmisVariable.class, 200);
  }

  @Override
  protected Stream<int[]> innerApply(NmisResponseFrame frame) {
    return frame.extractTime().mapToObj(value -> new int[] {value});
  }
}
