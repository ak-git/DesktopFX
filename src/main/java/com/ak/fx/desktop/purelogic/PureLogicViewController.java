package com.ak.fx.desktop.purelogic;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

import static com.ak.comm.converter.purelogic.PureLogicConverter.FREQUENCY;

@Named
@Profile("purelogic")
public final class PureLogicViewController extends AbstractScheduledViewController<PureLogicFrame, PureLogicFrame, PureLogicVariable> {
  private static final PureLogicFrame.StepCommand[] PINGS = EnumSet.complementOf(EnumSet.of(PureLogicFrame.StepCommand.MICRON_450))
      .toArray(PureLogicFrame.StepCommand[]::new);

  @Inject
  @ParametersAreNonnullByDefault
  public PureLogicViewController(Provider<BytesInterceptor<PureLogicFrame, PureLogicFrame>> interceptorProvider,
                                 Provider<Converter<PureLogicFrame, PureLogicVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, new Supplier<>() {
      private final Random random = new Random();
      private final Queue<PureLogicFrame.StepCommand> frames = new LinkedList<>();
      private boolean up = true;

      @Override
      public PureLogicFrame get() {
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
    }, FREQUENCY);
  }

  @Override
  public void up() {
    service().write(PureLogicFrame.StepCommand.MICRON_450.action(true));
  }

  @Override
  public void down() {
    service().write(PureLogicFrame.StepCommand.MICRON_450.action(false));
  }
}
