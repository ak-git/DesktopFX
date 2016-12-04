package com.ak.comm.converter.rsce;

import java.util.stream.Stream;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.Converter;

public final class RsceConverter implements Converter<RsceCommandFrame> {
  @Override
  public Stream<int[]> apply(RsceCommandFrame frame) {
    if (frame.hasResistance()) {
      return Stream.of(new int[] {frame.getR1DozenMilliOhms(), frame.getR2DozenMilliOhms()});
    }
    else {
      return Stream.empty();
    }
  }
}
