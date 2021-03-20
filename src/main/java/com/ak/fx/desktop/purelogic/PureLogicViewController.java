package com.ak.fx.desktop.purelogic;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Named;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.purelogic.PureLogicConverter;
import com.ak.comm.converter.purelogic.PureLogicVariable;
import com.ak.comm.interceptor.purelogic.PureLogicBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import org.springframework.context.annotation.Profile;

import static com.ak.comm.converter.purelogic.PureLogicConverter.FREQUENCY;

@Named
@Profile("purelogic")
public final class PureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand[] PINGS = EnumSet
      .of(
          PureLogicFrame.StepCommand.MICRON_090,
          PureLogicFrame.StepCommand.MICRON_150
      )
      .toArray(PureLogicFrame.StepCommand[]::new);
  private final Random random = new SecureRandom();
  private final Queue<PureLogicFrame.StepCommand> frames = new LinkedList<>();
  private final AtomicInteger handDirection = new AtomicInteger();
  private boolean up = true;
  private boolean isRefresh;

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
  public PureLogicFrame get() {
    if (isRefresh) {
      isRefresh = false;
      up = true;
      frames.clear();
    }

    if (up) {
      int hand = handDirection.get();
      if (hand > 0) {
        return PureLogicFrame.StepCommand.MICRON_450.action(handDirection.getAndDecrement() > 0);
      }
      else if (hand < 0) {
        return PureLogicFrame.StepCommand.MICRON_450.action(handDirection.getAndIncrement() > 0);
      }
    }

    if (frames.isEmpty()) {
      frames.addAll(
          random.ints(0, PINGS.length).distinct().limit(PINGS.length)
              .mapToObj(value -> PINGS[value]).collect(Collectors.toList())
      );
    }
    PureLogicFrame action = frames.element().action(up);
    if (!up) {
      frames.remove();
    }
    up = !up;
    return action;
  }

  @Override
  public void refresh() {
    isRefresh = true;
    super.refresh();
  }
}
