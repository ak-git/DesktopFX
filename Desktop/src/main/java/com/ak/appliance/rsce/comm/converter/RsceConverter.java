package com.ak.appliance.rsce.comm.converter;

import com.ak.appliance.rsce.comm.bytes.RsceCommandFrame;
import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

public final class RsceConverter extends AbstractConverter<RsceCommandFrame, RsceVariable> {
  private int r1;
  private int r2;
  private int accelerometer;
  private int openPercent;
  private int rotatePercent;

  public RsceConverter() {
    super(RsceVariable.class, 125);
  }

  @Override
  protected Stream<int[]> innerApply(RsceCommandFrame frame) {
    r1 = frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, r1);
    r2 = frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, r2);
    accelerometer = frame.extract(RsceCommandFrame.FrameField.ACCELEROMETER, accelerometer);
    openPercent = frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, openPercent);
    rotatePercent = frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, rotatePercent);
    return Stream.of(new int[] {r1, r2, accelerometer, openPercent, rotatePercent, frame.extract(RsceCommandFrame.FrameField.FINGER, 0)});
  }
}
