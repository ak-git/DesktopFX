package com.ak.fx.desktop.purelogic;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
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
  private static final PureLogicFrame[] PINGS = {
      PureLogicFrame.StepCommand.MICRON_150.action(true), PureLogicFrame.StepCommand.MICRON_150.action(false)
  };

  @Inject
  public PureLogicViewController(@Nonnull Provider<BytesInterceptor<PureLogicFrame, PureLogicFrame>> interceptorProvider,
                                 @Nonnull Provider<Converter<PureLogicFrame, PureLogicVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, new Supplier<>() {
      private int pingIndex = -1;

      @Override
      public PureLogicFrame get() {
        return PINGS[(++pingIndex) % PINGS.length];
      }
    }, FREQUENCY);
  }
}
