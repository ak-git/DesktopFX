package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import com.ak.appliance.purelogic.comm.converter.PureLogicConverter;
import com.ak.appliance.purelogic.comm.converter.PureLogicVariable;
import com.ak.appliance.purelogic.comm.interceptor.PureLogicBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ak.appliance.purelogic.comm.bytes.PureLogicFrame.StepCommand.MICRON_090;

abstract class AbstractPureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand[] AUTO_SEQUENCE = {
      MICRON_090, MICRON_090
  };
  private final AtomicInteger handDirection = new AtomicInteger();
  private boolean isRefresh;
  private boolean isStop;
  private int autoSequenceIndex = -1;

  AbstractPureLogicViewController(PureLogicAxisFrequency axisFrequency) {
    super(
        () -> new PureLogicBytesInterceptor("PureLogic%s".formatted(axisFrequency.name())),
        () -> new PureLogicConverter(axisFrequency),
        axisFrequency.value()
    );
  }

  @Override
  public final void up() {
    handDirection.incrementAndGet();
  }

  @Override
  public final void down() {
    handDirection.decrementAndGet();
  }

  @Override
  public final void left() {
    isStop = false;
  }

  @Override
  public final void right() {
    left();
  }

  @Override
  public final void escape() {
    handDirection.set(0);
    isRefresh = false;
    isStop = true;
    autoSequenceIndex = -1;
  }

  @Override
  public final PureLogicFrame get() {
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
        return MICRON_090.action(direction);
      }
      else {
        handDirection.getAndAdd(-delta);
      }
    }
    else if (isStop) {
      return PureLogicFrame.ALIVE;
    }
    return AUTO_SEQUENCE[autoSequenceIndex].action(!sequenceDirection);
  }

  @Override
  public final void refresh(boolean force) {
    isRefresh = true;
    super.refresh(force);
  }
}
