package com.ak.comm.converter.rcm;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;

public final class RcmConverter extends AbstractConverter<BufferFrame, RcmInVariable> {
  public RcmConverter() {
    super(RcmInVariable.class, 200);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    int[] values = new int[variables().size()];
    for (int i = 0; i < values.length; i++) {
      int index = 2 * i * Byte.BYTES;
      values[i] = ((frame.get(index) & 0x7E) << 5) + ((frame.get(index + 1) & 0x7E) >> 1);
    }
    return Stream.of(values);
  }
}
