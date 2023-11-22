package com.ak.fx.desktop.purelogic;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.purelogic.PureLogicConverter;
import com.ak.comm.converter.purelogic.PureLogicVariable;
import com.ak.comm.interceptor.purelogic.PureLogicBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_150;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_750;
import static com.ak.comm.converter.purelogic.PureLogicConverter.FREQUENCY;

@Component
@Profile("purelogic")
public final class PureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand[] AUTO_SEQUENCE = {
      MICRON_150, MICRON_150
  };
  private final AtomicInteger handDirection = new AtomicInteger();
  private boolean isRefresh;
  private int autoSequenceIndex = -1;

  public PureLogicViewController() {
    super(PureLogicBytesInterceptor::new, PureLogicConverter::new, FREQUENCY);
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
    isRefresh = false;
    autoSequenceIndex = -1;
  }

  @Override
  public PureLogicFrame get() {
    if (isRefresh) {
      escape();
    }

    autoSequenceIndex++;
    autoSequenceIndex %= AUTO_SEQUENCE.length;
    boolean sequenceDirection = (autoSequenceIndex & 1) == 0;

    int hand = handDirection.get();
    if (hand != 0) {
      int delta = hand > 0 ? -1 : 1;
      boolean direction = handDirection.getAndAdd(delta) > 0;
      if (sequenceDirection) {
        autoSequenceIndex = -1;
        return MICRON_750.action(direction);
      }
      else {
        handDirection.getAndAdd(-delta);
      }
    }
    return AUTO_SEQUENCE[autoSequenceIndex].action(!sequenceDirection);
  }

  @Override
  public void refresh(boolean force) {
    isRefresh = true;
    super.refresh(force);
  }
}
