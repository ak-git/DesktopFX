package com.ak.comm.converter.rsce;

import java.util.stream.Stream;

import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.AbstractConverter;
import com.ak.digitalfilter.Frequencies;

public final class RsceConverter extends AbstractConverter<RsceCommandFrame, RsceVariable> {
  public RsceConverter() {
    super(RsceVariable.class, Frequencies.HZ_200);
  }

  @Override
  protected Stream<int[]> innerApply(RsceCommandFrame frame) {
    int[] ints = frame.getRDozenMilliOhms().toArray();
    if (ints.length == RsceVariable.values().length) {
      return Stream.of(ints);
    }
    else {
      return Stream.empty();
    }
  }
}
