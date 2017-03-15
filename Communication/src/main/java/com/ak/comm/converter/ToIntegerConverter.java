package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;

import static java.lang.Integer.BYTES;

public final class ToIntegerConverter<EV extends Enum<EV> & Variable> extends AbstractConverter<BufferFrame, EV> {
  public ToIntegerConverter(@Nonnull Class<EV> evClass) {
    super(evClass);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    int[] values = new int[variables().size()];
    for (int i = 0; i < values.length; i++) {
      values[i] = frame.getInt(1 + i * BYTES);
    }
    return Stream.of(values);
  }
}
