package com.ak.comm.converter.purelogic;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;

public final class PureLogicConverter extends AbstractConverter<PureLogicFrame, PureLogicVariable> {
  public static final int FREQUENCY = 1;
  private static final int DATA_FREQUENCY = FREQUENCY * 100;
  private int position;

  public PureLogicConverter() {
    super(PureLogicVariable.class, DATA_FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull PureLogicFrame response) {
    position += response.getMicrons();
    return Stream.generate(() -> new int[] {position}).limit(DATA_FREQUENCY);
  }

  @Override
  public void refresh() {
    position = 0;
    super.refresh();
  }
}
