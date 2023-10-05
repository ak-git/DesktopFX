package com.ak.fx.desktop.sktb;

import com.ak.comm.bytes.sktbpr.SKTBRequest;
import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.converter.sktbpr.SKTBConverter;
import com.ak.comm.converter.sktbpr.SKTBVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import com.ak.fx.desktop.nmisr.RsceEvent;
import com.ak.util.Numbers;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.ak.comm.bytes.sktbpr.SKTBRequest.MAX_ROTATE_VELOCITY;


@Component
@Profile("sktb-pr")
public final class SKTBViewController extends AbstractScheduledViewController<SKTBRequest, SKTBResponse, SKTBVariable> {
  private final AtomicReference<SKTBRequest> sktbRequestRC = new AtomicReference<>(SKTBRequest.NONE);
  private final AtomicInteger rotateAngle = new AtomicInteger(0);
  private final AtomicInteger rotateVelocity = new AtomicInteger(0);

  @Inject
  @ParametersAreNonnullByDefault
  public SKTBViewController(Provider<BytesInterceptor<SKTBRequest, SKTBResponse>> interceptorProvider,
                            Provider<Converter<SKTBResponse, SKTBVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, SKTBConverter.FREQUENCY);
  }

  @Override
  @Nullable
  public SKTBRequest get() {
    SKTBRequest request = new SKTBRequest.RequestBuilder(sktbRequestRC.get()).rotate(rotateVelocity.get()).build();
    sktbRequestRC.set(request);
    return request;
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    int error = rotateAngle.get() - ints[SKTBVariable.ROTATE.ordinal()];
    error = Numbers.toInt(Math.min(Math.abs(error), MAX_ROTATE_VELOCITY) * Math.signum(error));
    rotateVelocity.set(error / 2);
  }

  @Override
  public void left() {
    rotateAngle.addAndGet(-10);
  }

  @Override
  public void right() {
    rotateAngle.addAndGet(10);
  }

  @Override
  public void escape() {
    rotateAngle.set(0);
  }

  @EventListener(RsceEvent.class)
  private void rsceEvent(@Nonnull RsceEvent event) {
    Logger.getLogger(getClass().getName()).info(() -> Integer.toString(event.getValue(RsceVariable.OPEN)));
  }
}
