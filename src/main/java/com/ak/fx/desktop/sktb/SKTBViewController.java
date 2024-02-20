package com.ak.fx.desktop.sktb;

import com.ak.comm.bytes.sktbpr.SKTBRequest;
import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.converter.sktbpr.SKTBConverter;
import com.ak.comm.converter.sktbpr.SKTBVariable;
import com.ak.comm.interceptor.sktbpr.SKTBBytesInterceptor;
import com.ak.fx.desktop.AbstractScheduledViewController;
import com.ak.fx.desktop.nmisr.RsceEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("sktb-pr")
public final class SKTBViewController extends AbstractScheduledViewController<SKTBRequest, SKTBResponse, SKTBVariable>
    implements ApplicationListener<RsceEvent> {
  private final AtomicReference<SKTBRequest> sktbRequestPrev = new AtomicReference<>(SKTBRequest.NONE);
  private final Map<SKTBVariable, SKTBAngleVelocityControl> controls = EnumSet.allOf(SKTBVariable.class).stream()
      .collect(
          Collectors.toMap(
              Function.identity(),
              SKTBAngleVelocityControl::new,
              (left, right) -> {
                throw new IllegalArgumentException("Duplicate keys %s and %s.".formatted(left, right));
              },
              () -> new EnumMap<>(SKTBVariable.class)
          )
      );

  public SKTBViewController() {
    super(SKTBBytesInterceptor::new, SKTBConverter::new, SKTBConverter.FREQUENCY);
  }

  @Override
  public SKTBRequest get() {
    SKTBRequest request = sktbRequestPrev.get().from()
        .rotate(controls.get(SKTBVariable.ROTATE).velocity())
        .flex(controls.get(SKTBVariable.FLEX).velocity())
        .grip(0).build();
    sktbRequestPrev.set(request);
    return request;
  }

  @Override
  public void onNext(int[] ints) {
    super.onNext(ints);
    controls.values().forEach(c -> c.accept(ints));
  }

  @Override
  public void left() {
    action(SKTBVariable.ROTATE, SKTBAngleVelocityControl::increment);
  }

  @Override
  public void right() {
    action(SKTBVariable.ROTATE, SKTBAngleVelocityControl::decrement);
  }

  @Override
  public void up() {
    action(SKTBVariable.FLEX, SKTBAngleVelocityControl::decrement);
  }

  @Override
  public void down() {
    action(SKTBVariable.FLEX, SKTBAngleVelocityControl::increment);
  }

  @Override
  public void escape() {
    controls.values().forEach(SKTBAngleVelocityControl::escape);
  }

  @Override
  @EventListener(RsceEvent.class)
  public void onApplicationEvent(RsceEvent rsceEvent) {
    controls.forEach((variable, control) -> control.update(rsceEvent));
  }

  private void action(SKTBVariable variable, Consumer<SKTBAngleVelocityControl> control) {
    control.accept(controls.get(variable));
  }
}
