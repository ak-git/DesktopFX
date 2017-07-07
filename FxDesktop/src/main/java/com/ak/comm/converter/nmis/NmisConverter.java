package com.ak.comm.converter.nmis;

import java.util.stream.Stream;

import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.converter.AbstractConverter;
import com.ak.digitalfilter.Frequencies;

public final class NmisConverter extends AbstractConverter<NmisResponseFrame, NmisVariable> {
  public NmisConverter() {
    super(NmisVariable.class, Frequencies.HZ_200);
  }

  @Override
  protected Stream<int[]> innerApply(NmisResponseFrame frame) {
    return frame.extractTime().mapToObj(value -> new int[] {value});
  }
}
