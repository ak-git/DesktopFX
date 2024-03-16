package com.ak.appliance.nmisr.fx.desktop;

import com.ak.appliance.nmis.comm.bytes.NmisRequest;
import com.ak.appliance.nmisr.comm.interceptor.NmisRsceBytesInterceptor;
import com.ak.appliance.rsce.comm.bytes.RsceCommandFrame;
import com.ak.appliance.rsce.comm.converter.RsceConverter;
import com.ak.appliance.rsce.comm.converter.RsceVariable;
import com.ak.fx.desktop.AbstractScheduledViewController;
import jakarta.inject.Inject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("nmis-rsce")
public final class NmisRsceViewController extends AbstractScheduledViewController<NmisRequest, RsceCommandFrame, RsceVariable> {
  private static final NmisRequest.Sequence[] PINGS = {
      NmisRequest.Sequence.CATCH_100, NmisRequest.Sequence.CATCH_60, NmisRequest.Sequence.CATCH_30,
      NmisRequest.Sequence.ROTATE_100, NmisRequest.Sequence.ROTATE_60, NmisRequest.Sequence.ROTATE_30};
  private final ApplicationEventPublisher eventPublisher;
  private int pingIndex = -1;

  @Inject
  public NmisRsceViewController(ApplicationEventPublisher eventPublisher) {
    super(NmisRsceBytesInterceptor::new, RsceConverter::new, 1.0 / 8.0);
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
  }

  @Override
  public NmisRequest get() {
    return PINGS[(++pingIndex) % PINGS.length].build();
  }

  @Override
  public void onNext(int[] ints) {
    super.onNext(ints);
    eventPublisher.publishEvent(new RsceEvent(this, ints));
  }
}
