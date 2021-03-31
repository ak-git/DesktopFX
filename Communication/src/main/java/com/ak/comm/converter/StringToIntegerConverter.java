package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;

public final class StringToIntegerConverter<V extends Enum<V> & Variable<V>> extends AbstractConverter<BufferFrame, V> {
  public StringToIntegerConverter(@Nonnull Class<V> evClass, @Nonnegative int frequency) {
    super(evClass, frequency);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    int[] values = new int[variables().size()];
    for (int i = 0; i < values.length; i++) {
      StringBuilder sb = new StringBuilder(Integer.BYTES);
      for (int j = 0; j < Integer.BYTES; j++) {
        sb.append((char) frame.get(1 + i * Integer.BYTES * Byte.BYTES + j));
      }
      values[i] = Integer.parseInt(sb.toString(), 16);
    }
    return Stream.of(values);
  }
}
