package com.ak.comm.converter.briko;

import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;
import com.ak.comm.converter.Variable;

import static java.lang.Integer.BYTES;

public final class BrikoConverter<EV extends Enum<EV> & Variable<EV>> extends AbstractConverter<BufferFrame, EV> {
  public BrikoConverter(@Nonnull Class<EV> evClass, @Nonnegative int frequency) {
    super(evClass, frequency);
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
