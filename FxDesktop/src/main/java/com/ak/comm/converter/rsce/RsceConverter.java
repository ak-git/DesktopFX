package com.ak.comm.converter.rsce;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.AbstractConverter;

public final class RsceConverter extends AbstractConverter<RsceCommandFrame, RsceVariable> {
  public RsceConverter() {
    super(RsceVariable.class, 125);
  }

  @Override
  protected Stream<int[]> innerApply(RsceCommandFrame frame) {
    int[] ints = IntStream.concat(frame.getRDozenMilliOhms(), frame.getInfoOnes()).toArray();
    if (ints.length == RsceVariable.values().length) {
      return Stream.of(ints);
    }
    else {
      return Stream.empty();
    }
  }
}
