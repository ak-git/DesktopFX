package com.ak.comm.converter.prv_rr;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;

public final class PrvRRConverter extends AbstractConverter<BufferFrame, PrvRRInputVariable> {
  public PrvRRConverter() {
    super(PrvRRInputVariable.class, 200);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    var values = new int[2];
    values[0] = frame.getInt(0);
    values[1] = frame.getShort(4);
    return Stream.of(values);
  }
}
