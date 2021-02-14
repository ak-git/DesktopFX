package com.ak.comm.converter.purelogic;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;

public final class PureLogicConverter extends AbstractConverter<PureLogicFrame, PureLogicVariable> {
  public static final int FREQUENCY = 1;

  public PureLogicConverter() {
    super(PureLogicVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull PureLogicFrame response) {
    return Stream.of(new int[] {response.getMicrons()});
  }
}
