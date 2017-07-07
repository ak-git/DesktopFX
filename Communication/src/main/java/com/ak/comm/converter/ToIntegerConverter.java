package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;

import com.ak.comm.bytes.BufferFrame;

import static java.lang.Integer.BYTES;

public final class ToIntegerConverter<EV extends Enum<EV> & Variable<EV>> extends AbstractConverter<BufferFrame, EV> {
  public ToIntegerConverter(@Nonnull Class<EV> evClass, @Nonnull Quantity<Frequency> frequency) {
    super(evClass, frequency);
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
