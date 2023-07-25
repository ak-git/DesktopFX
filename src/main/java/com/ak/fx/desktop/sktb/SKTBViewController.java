package com.ak.fx.desktop.sktb;

import com.ak.comm.bytes.sktbpr.SKTBRequest;
import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.sktbpr.SKTBConverter;
import com.ak.comm.converter.sktbpr.SKTBVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


@Component
@Profile("sktb-pr")
public final class SKTBViewController extends AbstractScheduledViewController<SKTBRequest, SKTBResponse, SKTBVariable> {
  private final AtomicReference<SKTBRequest> sktbRequestRC = new AtomicReference<>(SKTBRequest.NONE);
  private final AtomicInteger rotate = new AtomicInteger(0);
  private final AtomicInteger flex = new AtomicInteger(0);

  @Inject
  @ParametersAreNonnullByDefault
  public SKTBViewController(Provider<BytesInterceptor<SKTBRequest, SKTBResponse>> interceptorProvider,
                            Provider<Converter<SKTBResponse, SKTBVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, SKTBConverter.FREQUENCY);
  }

  @Override
  public SKTBRequest get() {
    SKTBRequest request = new SKTBRequest.RequestBuilder(sktbRequestRC.get()).rotate(rotate.get()).build();
    sktbRequestRC.set(request);
    return request;
  }

  @Override
  public void up() {
    rotate.addAndGet(2);
    flex.addAndGet(-1);
  }

  @Override
  public void down() {
    rotate.addAndGet(-2);
    flex.addAndGet(1);
  }

  @Override
  public void escape() {
    rotate.set(0);
    flex.set(0);
  }
}
