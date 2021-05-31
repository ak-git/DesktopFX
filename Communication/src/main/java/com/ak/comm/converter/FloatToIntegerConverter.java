package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;

public final class FloatToIntegerConverter<V extends Enum<V> & Variable<V>> extends AbstractConverter<BufferFrame, V> {
  public FloatToIntegerConverter(@Nonnull Class<V> evClass, @Nonnegative int frequency) {
    super(evClass, frequency);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = Math.round(frame.getFloat(1 + i * Float.BYTES));
    }
    return Stream.of(values);
  }
}
