package com.ak.comm.converter.purelogic;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.context.annotation.Profile;

@Named
@Profile("purelogic")
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
    int[] data = new int[DATA_FREQUENCY];
    Arrays.fill(data, position);
    return Stream.of(data);
  }
}
