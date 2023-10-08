package com.ak.fx.desktop.sktb;

import com.ak.comm.bytes.sktbpr.SKTBRequest;
import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.converter.Converter;
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
import java.security.SecureRandom;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;


@Component
@Profile("sktb-pr")
public final class SKTBViewController extends AbstractScheduledViewController<SKTBRequest, SKTBResponse, SKTBVariable>
    implements ApplicationListener<RsceEvent> {
  private final AtomicReference<SKTBRequest> sktbRequestPrev = new AtomicReference<>(SKTBRequest.NONE);
  private final List<SKTBAngleVelocityControl> controls = EnumSet.allOf(SKTBVariable.class).stream()
      .map(SKTBAngleVelocityControl::new).toList();
  private final Collection<SKTBVariable> pushFlags = new CopyOnWriteArraySet<>();
  private final RandomGenerator random = new SecureRandom();

  @Inject
  @ParametersAreNonnullByDefault
  public SKTBViewController(Provider<BytesInterceptor<SKTBRequest, SKTBResponse>> interceptorProvider,
                            Provider<Converter<SKTBResponse, SKTBVariable>> converterProvider) {
    super(interceptorProvider, converterProvider, SKTBConverter.FREQUENCY);
  }

  @Override
  @Nullable
  public SKTBRequest get() {
    SKTBVariable[] allSKTBVariables = SKTBVariable.values();
    SKTBAngleVelocityControl peek = controls.get(pushFlags.stream().findFirst()
        .orElse(allSKTBVariables[random.nextInt(allSKTBVariables.length)]).ordinal());
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
    action(SKTBVariable.ROTATE, SKTBAngleVelocityControl::decrement);
  }

  @Override
  public void right() {
    action(SKTBVariable.ROTATE, SKTBAngleVelocityControl::increment);
  }

  @Override
  public void up() {
    action(SKTBVariable.FLEX, SKTBAngleVelocityControl::increment);
  }

  @Override
  public void down() {
    action(SKTBVariable.FLEX, SKTBAngleVelocityControl::decrement);
  }

  @Override
  public void escape() {
    controls.forEach(SKTBAngleVelocityControl::escape);
    pushFlags.addAll(EnumSet.allOf(SKTBVariable.class));
  }

  @Override
  @EventListener(RsceEvent.class)
  public void onApplicationEvent(@Nonnull RsceEvent rsceEvent) {
    pushFlags.addAll(
        controls.stream()
            .filter(c -> c.isUpdatedBy(rsceEvent.getValue(c.rsceMapping())))
            .map(SKTBAngleVelocityControl::variable)
            .toList()
    );
  }

  @ParametersAreNonnullByDefault
  private void action(SKTBVariable variable, Consumer<SKTBAngleVelocityControl> control) {
    control.accept(controls.get(variable.ordinal()));
    pushFlags.add(variable);
  }
}
