package com.ak.comm.converter.rsce;

import java.util.stream.Stream;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.AbstractConverter;

public final class RsceConverter extends AbstractConverter<RsceCommandFrame, RsceVariable> {
  public RsceConverter() {
    super(RsceVariable.class);
  }

  @Override
  protected Stream<int[]> innerApply(RsceCommandFrame frame) {
    if (frame.hasResistance()) {
      return Stream.of(new int[] {frame.getR1DozenMilliOhms(), frame.getR2DozenMilliOhms()});
    }
    else {
      return Stream.empty();
    }
  }
}
