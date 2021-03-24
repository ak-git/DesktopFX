package com.ak.fx.desktop.purelogic;

import java.security.SecureRandom;
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

import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_090;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_150;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_450;
import static com.ak.comm.converter.purelogic.PureLogicConverter.FREQUENCY;

@Named
@Profile("purelogic")
public final class PureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand[] PINGS = {MICRON_090, MICRON_150};
  private static final PureLogicFrame.StepCommand[] AUTO_SEQUENCE = {MICRON_150, MICRON_150, MICRON_150, MICRON_450};
  private final Random random = new SecureRandom();
  private final Queue<PureLogicFrame.StepCommand> frames = new LinkedList<>();
  private final AtomicInteger handDirection = new AtomicInteger();
  private final AtomicInteger autoDirection = new AtomicInteger();
  private boolean up = true;
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
    autoDirection.set(0);
    up = true;
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
        return MICRON_450.action(direction > 0);
      }
      else if (autoDirection.get() != 0) {
        autoSequenceIndex++;
        autoSequenceIndex %= AUTO_SEQUENCE.length;
        boolean sequenceDirection = (autoSequenceIndex & 1) != 0;
        if (autoDirection.get() < 0) {
          sequenceDirection = !sequenceDirection;
        }
        return AUTO_SEQUENCE[autoSequenceIndex].action(sequenceDirection);
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
