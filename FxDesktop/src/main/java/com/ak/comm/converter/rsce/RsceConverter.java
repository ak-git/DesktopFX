package com.ak.comm.converter.rsce;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.AbstractConverter;

public final class RsceConverter extends AbstractConverter<RsceCommandFrame, RsceVariable> {
  private int catchPercent;
  private int rotatePercent;

  public RsceConverter() {
    super(RsceVariable.class, 125);
  }

  @Override
  protected Stream<int[]> innerApply(RsceCommandFrame frame) {
    catchPercent = frame.getCatchPercent(catchPercent);
    rotatePercent = frame.getRotatePercent(rotatePercent);
    int[] ints = IntStream.concat(IntStream.concat(frame.getRDozenMilliOhms(), frame.getInfoOnes()),
        IntStream.of(catchPercent, rotatePercent)).toArray();
    if (ints.length == RsceVariable.values().length) {
      return Stream.of(ints);
    }
    else {
      return Stream.empty();
    }
  }
}
