package com.ak.appliance.purelogic.comm.converter;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

public final class PureLogicConverter extends AbstractConverter<PureLogicFrame, PureLogicVariable> {
  public static final int FREQUENCY = 2;
  private static final int DATA_FREQUENCY = 1000;
  private int position;

  public PureLogicConverter() {
    super(PureLogicVariable.class, DATA_FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(PureLogicFrame response) {
    position += response.getMicrons();
    return Stream.generate(() -> new int[] {position}).limit(DATA_FREQUENCY / FREQUENCY);
  }

  @Override
  public void refresh(boolean force) {
    position = 0;
    super.refresh(force);
  }
}
