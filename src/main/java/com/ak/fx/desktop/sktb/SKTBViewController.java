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
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;


@Component
@Profile("sktb-pr")
public final class SKTBViewController extends AbstractScheduledViewController<SKTBRequest, SKTBResponse, SKTBVariable>
    implements ApplicationListener<RsceEvent> {
  @Nonnull
  private final AtomicReference<SKTBRequest> sktbRequestPrev = new AtomicReference<>(SKTBRequest.NONE);
  @Nonnull
  private final List<SKTBAngleVelocityControl> controls = EnumSet.allOf(SKTBVariable.class).stream()
      .map(SKTBAngleVelocityControl::new).toList();
  @Nonnull
  private final Collection<SKTBVariable> pushFlags = new CopyOnWriteArraySet<>();

  @Inject
  @ParametersAreNonnullByDefault
  public SKTBViewController(Provider<BytesInterceptor<SKTBRequest, SKTBResponse>> interceptorProvider,
                            Provider<Converter<SKTBResponse, SKTBVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, SKTBConverter.FREQUENCY);
  }

  @Override
  @Nullable
  public SKTBRequest get() {
    SKTBAngleVelocityControl peek = controls.get(pushFlags.stream().findFirst().orElse(SKTBVariable.ROTATE).ordinal());
    SKTBRequest.RequestBuilder requestBuilder = new SKTBRequest.RequestBuilder(sktbRequestPrev.get());
    var request = switch (peek.variable()) {
      case ROTATE -> requestBuilder.rotate(peek.velocity()).build();
      case FLEX -> requestBuilder.flex(peek.velocity()).build();
    };
    sktbRequestPrev.set(request);
    return request;
  }

  @Override
  public void onNext(@Nonnull int[] ints) {
    super.onNext(ints);
    controls.forEach(c -> c.accept(ints));
  }

  @Override
  public void left() {
    controls.get(SKTBVariable.ROTATE.ordinal()).decrement();
    pushFlags.add(SKTBVariable.ROTATE);
  }

  @Override
  public void right() {
    controls.get(SKTBVariable.ROTATE.ordinal()).increment();
    pushFlags.add(SKTBVariable.ROTATE);
  }

  @Override
  public void up() {
    controls.get(SKTBVariable.FLEX.ordinal()).increment();
    pushFlags.add(SKTBVariable.FLEX);
  }

  @Override
  public void down() {
    controls.get(SKTBVariable.FLEX.ordinal()).decrement();
    pushFlags.add(SKTBVariable.FLEX);
  }

  @Override
  public void escape() {
    controls.forEach(SKTBAngleVelocityControl::escape);
    pushFlags.addAll(EnumSet.allOf(SKTBVariable.class));
  }

  @Override
  @EventListener(RsceEvent.class)
  public void onApplicationEvent(@Nonnull RsceEvent event) {
    Logger.getLogger(getClass().getName()).info(() -> Integer.toString(event.getValue(RsceVariable.OPEN)));
  }
}
