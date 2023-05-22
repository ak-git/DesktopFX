package com.ak.comm.converter.purelogic;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

@Component
@Profile("purelogic")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class PureLogicConverter extends AbstractConverter<PureLogicFrame, PureLogicVariable> {
  public static final int FREQUENCY = 4;
  private static final int DATA_FREQUENCY = 1000;
  private int position;

  public PureLogicConverter() {
    super(PureLogicVariable.class, DATA_FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull PureLogicFrame response) {
    position += response.getMicrons();
    return Stream.generate(() -> new int[] {position}).limit(DATA_FREQUENCY / FREQUENCY);
  }

  @Override
  public void refresh(boolean force) {
    position = 0;
    super.refresh(force);
  }
}
