package com.ak.fx.desktop.nmisr;

import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.fx.desktop.AbstractViewController;

public final class NmisRsceViewController extends AbstractViewController<NmisRequest, RsceCommandFrame, RsceVariable> implements Closeable {
  private static final NmisRequest.Sequence[] PINGS = {
      NmisRequest.Sequence.CATCH_100, NmisRequest.Sequence.CATCH_60, NmisRequest.Sequence.CATCH_30,
      NmisRequest.Sequence.ROTATE_100, NmisRequest.Sequence.ROTATE_60, NmisRequest.Sequence.ROTATE_30};
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private int pingIndex = -1;

  @Inject
  public NmisRsceViewController(@Nonnull GroupService<NmisRequest, RsceCommandFrame, RsceVariable> service) {
    super(service);
    executorService.scheduleAtFixedRate(() -> service.write(PINGS[(++pingIndex) % PINGS.length].build()), 0, 8, TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    executorService.shutdownNow();
  }
}
