package com.ak.fx.desktop.nmisr;

import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.rsce.RsceConverter;
import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.interceptor.nmisr.NmisRsceBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import jakarta.inject.Inject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@Profile("nmis-rsce")
public final class NmisRsceViewController extends AbstractScheduledViewController<NmisRequest, RsceCommandFrame, RsceVariable> {
  private static final NmisRequest.Sequence[] PINGS = {
      NmisRequest.Sequence.CATCH_100, NmisRequest.Sequence.CATCH_60, NmisRequest.Sequence.CATCH_30,
      NmisRequest.Sequence.ROTATE_100, NmisRequest.Sequence.ROTATE_60, NmisRequest.Sequence.ROTATE_30};
  @Nonnull
  private final ApplicationEventPublisher eventPublisher;
  private int pingIndex = -1;

  @Inject
  public NmisRsceViewController(@Nonnull ApplicationEventPublisher eventPublisher) {
    super(NmisRsceBytesInterceptor::new, RsceConverter::new, 1.0 / 8.0);
    this.eventPublisher = eventPublisher;
  }

  @Override
  public NmisRequest get() {
    return PINGS[(++pingIndex) % PINGS.length].build();
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    eventPublisher.publishEvent(new RsceEvent(this, ints));
  }
}
