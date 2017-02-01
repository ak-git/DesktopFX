package com.ak.comm.converter.nmis;

import java.util.stream.Stream;

import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.converter.AbstractConverter;

public final class NmisConverter extends AbstractConverter<NmisResponseFrame, NmisVariable> {
  public NmisConverter() {
    super(NmisVariable.class);
  }

  @Override
  protected Stream<int[]> innerApply(NmisResponseFrame frame) {
    return Stream.of(new int[] {frame.getShort(3), frame.getShort(0)});
  }
}
