package com.ak.fx.desktop.purelogic;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.purelogic.PureLogicVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_015;
import static com.ak.comm.bytes.purelogic.PureLogicFrame.StepCommand.MICRON_1050;
import static com.ak.comm.converter.purelogic.PureLogicConverter.FREQUENCY;

@Component
@Profile("purelogic")
public final class PureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand[] AUTO_SEQUENCE = {
      MICRON_015, MICRON_015
  };
  private final AtomicInteger handDirection = new AtomicInteger();
  private boolean up;
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
    up = false;
    isRefresh = false;
    autoSequenceIndex = -1;
  }

  @Override
  public PureLogicFrame get() {
    if (isRefresh) {
      escape();
    }

    int hand = handDirection.get();
    if (hand == 0) {
      autoSequenceIndex++;
      if (autoSequenceIndex == AUTO_SEQUENCE.length) {
        up = !up;
      }
      autoSequenceIndex %= AUTO_SEQUENCE.length;
      boolean sequenceDirection = (autoSequenceIndex & 1) == 0;
      if (up) {
        return AUTO_SEQUENCE[AUTO_SEQUENCE.length - autoSequenceIndex - 1].action(!sequenceDirection);
      }
      else {
        return AUTO_SEQUENCE[autoSequenceIndex].action(sequenceDirection);
      }
    }
    else {
      int direction = handDirection.getAndAdd(hand > 0 ? -1 : 1);
      up = direction > 0;
      autoSequenceIndex = -1;
      return MICRON_1050.action(direction > 0);
    }
  }

  @Override
  public void refresh(boolean force) {
    isRefresh = true;
    super.refresh(force);
  }
}
