package com.ak.comm.converter.briko;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;

import static java.lang.Integer.BYTES;

public final class BrikoConverter extends AbstractConverter<BufferFrame, BrikoVariable> {
  public BrikoConverter() {
    super(BrikoVariable.class, 1000);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    int[] values = new int[variables().size()];
    for (int i = 0; i < values.length; i++) {
      values[i] = frame.getInt(2 + (i + 1) + i * BYTES);
    }
    return Stream.of(values);
  }
}
