package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import com.ak.appliance.purelogic.comm.converter.PureLogicConverter;
import com.ak.appliance.purelogic.comm.converter.PureLogicVariable;
import com.ak.appliance.purelogic.comm.interceptor.PureLogicBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

abstract class AbstractPureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private enum AutoSequence {
    AUTO_SEQUENCE_1(
        Stream.of(
                Stream.generate(() -> PureLogicFrame.ALIVE).limit(4),
                Stream.of(PureLogicFrame.Direction.UP.micron7p5multiplyBy(6)),
                Stream.iterate(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(12), PureLogicFrame::inverse).limit(5),
                Stream.of(PureLogicFrame.Direction.UP.micron7p5multiplyBy(6)),
                Stream.of(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(12))
            )
            .flatMap(Function.identity())
            .toList()
    ),
    AUTO_SEQUENCE_2(
        Stream.of(
                Stream.generate(() -> PureLogicFrame.Direction.UP.micron7p5multiplyBy(1)).limit(6),
                Stream.generate(() -> PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(1)).limit(12),
                Stream.generate(() -> PureLogicFrame.Direction.UP.micron7p5multiplyBy(1)).limit(6),
                Stream.of(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(12))
            )
            .flatMap(Function.identity()).toList()
    );

    private final List<PureLogicFrame> sequences;

    AutoSequence(List<PureLogicFrame> sequences) {
      this.sequences = sequences;
    }
  }

  private final List<PureLogicFrame> autoSequence = new CopyOnWriteArrayList<>();

  private final AtomicReference<PureLogicFrame.Direction> direction = new AtomicReference<>(PureLogicFrame.Direction.NONE);
  private boolean isStop;
  private boolean isInverted;
  private int changeIndex;
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
    changeIndex = (changeIndex + 1) % AutoSequence.values().length;
  }

  @Override
  public final PureLogicFrame get() {
    if (autoSequenceIndex == autoSequence.size() - 1) {
      autoSequence.clear();
      autoSequence.addAll(AutoSequence.values()[changeIndex].sequences);
      autoSequenceIndex = autoSequence.size() - 1;

      PureLogicFrame.Direction d = direction.getAndSet(PureLogicFrame.Direction.NONE);
      boolean inv = (d == PureLogicFrame.Direction.UP);
      if (isStop) {
        isInverted = inv;
        return d.micron7p5multiplyBy(60);
      }
      else if (d != PureLogicFrame.Direction.NONE) {
        isInverted = inv;
        return d.micron7p5multiplyBy(12);
      }
    }

    autoSequenceIndex++;
    autoSequenceIndex %= autoSequence.size();
    PureLogicFrame pureLogicFrame = autoSequence.get(autoSequenceIndex);
    if (isInverted) {
      pureLogicFrame = pureLogicFrame.inverse();
    }
    return pureLogicFrame;
  }
}
