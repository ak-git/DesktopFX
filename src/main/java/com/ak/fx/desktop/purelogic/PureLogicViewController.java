package com.ak.fx.desktop.purelogic;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.purelogic.PureLogicVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import org.springframework.context.annotation.Profile;

import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_1050;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_210;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_420;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_840;
import static com.ak.comm.converter.purelogic.PureLogicConverter.FREQUENCY;

@Named
@Profile("purelogic")
public final class PureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand PING = MICRON_210;
  private static final PureLogicFrame.StepCommand[] AUTO_SEQUENCE = {
      MICRON_210, MICRON_210, MICRON_210, MICRON_210,
      MICRON_420, MICRON_420, MICRON_840, MICRON_840
  };
  private final Queue<PureLogicFrame.StepCommand> frames = new LinkedList<>();
  private final AtomicInteger handDirection = new AtomicInteger();
  private final AtomicInteger autoDirection = new AtomicInteger();
  private boolean up = true;
  private boolean isRefresh;
  private int autoSequenceIndex = -1;

  @Inject
  @ParametersAreNonnullByDefault
  public PureLogicViewController(Provider<BytesInterceptor<PureLogicFrame, PureLogicFrame>> interceptorProvider,
                                 Provider<Converter<PureLogicFrame, PureLogicVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, FREQUENCY);
  }

  @Override
  public void up() {
    handDirection.incrementAndGet();
  }

  @Override
  public void down() {
    handDirection.decrementAndGet();
  }

  @Override
  public void escape() {
    handDirection.set(0);
    autoDirection.set(0);
    isRefresh = false;
    autoSequenceIndex = -1;
    frames.clear();
  }

  @Override
  public PureLogicFrame get() {
    if (isRefresh) {
      escape();
    }

    if (up) {
      int hand = handDirection.get();
      if (hand != 0) {
        int direction = handDirection.getAndAdd(hand > 0 ? -1 : 1);
        autoDirection.set(direction);
        return MICRON_1050.action(direction > 0);
      }
    }

    if (autoDirection.get() != 0) {
      autoSequenceIndex++;
      if (autoSequenceIndex == AUTO_SEQUENCE.length) {
        autoDirection.set(-autoDirection.get());
      }
      autoSequenceIndex %= AUTO_SEQUENCE.length;
      boolean sequenceDirection = (autoSequenceIndex & 1) == 0;
      if (autoDirection.get() < 0) {
        sequenceDirection = !sequenceDirection;
      }
      return AUTO_SEQUENCE[autoSequenceIndex].action(sequenceDirection);
    }

    if (frames.isEmpty()) {
      frames.add(PING);
    }
    PureLogicFrame action = frames.element().action(up);
    if (!up) {
      frames.remove();
    }
    up = !up;
    return action;
  }

  @Override
  public void refresh(boolean force) {
    isRefresh = true;
    super.refresh(force);
  }
}
