package com.ak.fx.desktop.nmisr;

import javax.inject.Named;

import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.rsce.RsceConverter;
import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.interceptor.nmisr.NmisRsceBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import org.springframework.context.annotation.Profile;

@Named
@Profile("nmis-rsce")
public final class NmisRsceViewController extends AbstractScheduledViewController<NmisRequest, RsceCommandFrame, RsceVariable> {
  private static final NmisRequest.Sequence[] PINGS = {
      NmisRequest.Sequence.CATCH_100, NmisRequest.Sequence.CATCH_60, NmisRequest.Sequence.CATCH_30,
      NmisRequest.Sequence.ROTATE_100, NmisRequest.Sequence.ROTATE_60, NmisRequest.Sequence.ROTATE_30};
  private int pingIndex = -1;

  public NmisRsceViewController() {
    super(NmisRsceBytesInterceptor::new, RsceConverter::new, 1.0 / 8.0);
  }

  @Override
  public NmisRequest get() {
    return PINGS[(++pingIndex) % PINGS.length].build();
  }
}
