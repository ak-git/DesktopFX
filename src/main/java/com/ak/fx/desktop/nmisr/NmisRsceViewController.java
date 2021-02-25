package com.ak.fx.desktop.nmisr;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("nmis-rsce")
public final class NmisRsceViewController extends AbstractScheduledViewController<NmisRequest, RsceCommandFrame, RsceVariable> {
  private static final NmisRequest.Sequence[] PINGS = {
      NmisRequest.Sequence.CATCH_100, NmisRequest.Sequence.CATCH_60, NmisRequest.Sequence.CATCH_30,
      NmisRequest.Sequence.ROTATE_100, NmisRequest.Sequence.ROTATE_60, NmisRequest.Sequence.ROTATE_30};

  @Inject
  @ParametersAreNonnullByDefault
  public NmisRsceViewController(Provider<BytesInterceptor<NmisRequest, RsceCommandFrame>> interceptorProvider,
                                Provider<Converter<RsceCommandFrame, RsceVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, new Supplier<>() {
      private int pingIndex = -1;

      @Override
      public NmisRequest get() {
        return PINGS[(++pingIndex) % PINGS.length].build();
      }
    }, 1.0 / 8.0);
  }
}
