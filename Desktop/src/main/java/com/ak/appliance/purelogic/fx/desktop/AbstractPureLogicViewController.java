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
    AUTO_SEQUENCE_PULSE(
        Stream.of(
            Stream.generate(() -> PureLogicFrame.ALIVE).limit(5),
            Stream.of(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(6)),
            Stream.iterate(PureLogicFrame.Direction.UP.micron7p5multiplyBy(12), PureLogicFrame::inverse).limit(11),
            Stream.of(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(6)),
            Stream.generate(() -> PureLogicFrame.ALIVE).limit(2)
        )
    ),
    AUTO_SEQUENCE_LOW(
        Stream.of(
            Stream.generate(() -> PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(1)).limit(6),
            Stream.generate(() -> PureLogicFrame.Direction.UP.micron7p5multiplyBy(1)).limit(12),
            Stream.generate(() -> PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(1)).limit(6)
        )
    );

    private final List<PureLogicFrame> sequences;

    AutoSequence(Stream<Stream<PureLogicFrame>> sequences) {
      this.sequences = sequences.flatMap(Function.identity()).toList();
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
    direction.set(PureLogicFrame.Direction.NONE);
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
      PureLogicFrame.Direction d = direction.get();
      isInverted = d == PureLogicFrame.Direction.DOWN;

      if (isStop) {
        direction.set(PureLogicFrame.Direction.NONE);
        return d.micron7p5multiplyBy(60);
      }
      else if (d != PureLogicFrame.Direction.NONE) {
        autoSequenceIndex = -1;
        return d.micron7p5multiplyBy(24);
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
