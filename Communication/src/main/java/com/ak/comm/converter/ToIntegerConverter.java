package com.ak.comm.converter;

import com.ak.comm.bytes.BufferFrame;

import javax.annotation.Nonnegative;
import java.util.stream.Stream;

import static java.lang.Integer.BYTES;

public final class ToIntegerConverter<V extends Enum<V> & Variable<V>> extends AbstractConverter<BufferFrame, V> {
  public ToIntegerConverter(Class<V> evClass, @Nonnegative int frequency) {
    super(evClass, frequency);
  }

  @Override
  protected Stream<int[]> innerApply(BufferFrame frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = frame.getInt(1 + i * BYTES);
    }
    return Stream.of(values);
  }
}
