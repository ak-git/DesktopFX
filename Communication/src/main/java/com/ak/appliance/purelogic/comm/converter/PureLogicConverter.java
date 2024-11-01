package com.ak.appliance.purelogic.comm.converter;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;
import com.ak.util.Numbers;

import java.util.stream.Stream;

public final class PureLogicConverter extends AbstractConverter<PureLogicFrame, PureLogicVariable> {
  private static final int DATA_FREQUENCY = 1000;
  private final PureLogicAxisFrequency axisFrequency;
  private int position;

  public PureLogicConverter(PureLogicAxisFrequency axisFrequency) {
    super(PureLogicVariable.class, DATA_FREQUENCY);
    this.axisFrequency = axisFrequency;
  }

  @Override
  protected Stream<int[]> innerApply(PureLogicFrame response) {
    position += response.getMicrons();
    return Stream.generate(() -> new int[] {position}).limit(Numbers.toInt(DATA_FREQUENCY / axisFrequency.value()) + 2L);
  }

  @Override
  public void refresh(boolean force) {
    position = 0;
    super.refresh(force);
  }
}
