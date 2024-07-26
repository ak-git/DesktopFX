package com.ak.appliance.briko.comm.converter;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

import static com.ak.appliance.briko.comm.converter.BrikoVariable.FREQUENCY;

public final class BrikoConverter extends AbstractConverter<BufferFrame, BrikoVariable> {
  public BrikoConverter() {
    super(BrikoVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(BufferFrame frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      int index = 1 + (i * 4) + 1;
      for (int j = 0; j < 3; j++) {
        values[i] |= (frame.get(index + j) & 0xff) << (j * 8 + 8);
      }
      values[i] >>= 8;
    }
    return Stream.of(values);
  }
}
