package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import com.ak.appliance.purelogic.comm.converter.PureLogicConverter;
import com.ak.appliance.purelogic.comm.converter.PureLogicVariable;
import com.ak.appliance.purelogic.comm.interceptor.PureLogicBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;

import java.util.concurrent.atomic.AtomicReference;

abstract class AbstractPureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame[] AUTO_SEQUENCE = {
      PureLogicFrame.Direction.DOWN.micron15multiplyBy(6), PureLogicFrame.Direction.UP.micron15multiplyBy(6)
  };
  private final AtomicReference<PureLogicFrame.Direction> direction = new AtomicReference<>(PureLogicFrame.Direction.NONE);
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
    direction.set(PureLogicFrame.Direction.UP);
  }

  @Override
  public final void down() {
    direction.set(PureLogicFrame.Direction.DOWN);
  }

  @Override
  public final void left() {
    isStop = true;
  }

  @Override
  public final void right() {
    isStop = false;
  }

  @Override
  public final void escape() {
  }

  @Override
  public final PureLogicFrame get() {
    if (autoSequenceIndex == AUTO_SEQUENCE.length - 1) {
      PureLogicFrame.Direction d = direction.getAndSet(PureLogicFrame.Direction.NONE);
      if (isStop) {
        return d.micron15multiplyBy(30);
      }
      else if (d != PureLogicFrame.Direction.NONE) {
        return d.micron15multiplyBy(6);
      }
    }

    autoSequenceIndex++;
    autoSequenceIndex %= AUTO_SEQUENCE.length;
    return AUTO_SEQUENCE[autoSequenceIndex];
  }
}
