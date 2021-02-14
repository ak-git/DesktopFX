package com.ak.comm.converter.purelogic;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.context.annotation.Profile;

@Named
@Profile("purelogic")
public final class PureLogicConverter extends AbstractConverter<PureLogicFrame, PureLogicVariable> {
  public static final double FREQUENCY = 1;

  public PureLogicConverter() {
    super(PureLogicVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull PureLogicFrame response) {
    return Stream.of(new int[] {response.getMicrons()});
  }
}
