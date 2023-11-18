package com.ak.fx.desktop.sktb;

import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.converter.sktbpr.SKTBVariable;
import com.ak.fx.desktop.nmisr.RsceEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;

class SKTBAngleVelocityControlTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @ParameterizedTest
  @EnumSource(SKTBVariable.class)
  void accept(@Nonnull SKTBVariable variable) {
    SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);
    int[] inputValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();
    control.accept(inputValues);
    Assertions.assertThat(control.velocity()).isEqualTo((-inputValues[variable.ordinal()] / 2) * 1000);
  }

  @ParameterizedTest
  @EnumSource(SKTBVariable.class)
  void decrement(@Nonnull SKTBVariable variable) {
    SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);
    int[] inputValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();

    int maxCalls = RANDOM.nextInt(1, 10);
    for (int i = 0; i < maxCalls; i++) {
      control.decrement();
    }

    control.accept(inputValues);
    control.escape();
    Assertions.assertThat(control.velocity()).as(() -> "control.decrement() calls count = %d".formatted(maxCalls))
        .isEqualTo(((-10 * maxCalls - inputValues[variable.ordinal()]) / 2) * 1000);

    control.accept(inputValues);
    Assertions.assertThat(control.velocity()).isEqualTo((-inputValues[variable.ordinal()] / 2) * 1000);
  }

  @ParameterizedTest
  @EnumSource(SKTBVariable.class)
  void increment(@Nonnull SKTBVariable variable) {
    SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);
    int[] inputValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();

    int maxCalls = RANDOM.nextInt(1, 10);
    for (int i = 0; i < maxCalls; i++) {
      control.increment();
    }

    control.accept(inputValues);
    control.escape();
    Assertions.assertThat(control.velocity()).as(() -> "control.increment() calls count = %d".formatted(maxCalls))
        .isEqualTo(((10 * maxCalls - inputValues[variable.ordinal()]) / 2) * 1000);

    control.accept(inputValues);
    Assertions.assertThat(control.velocity()).isEqualTo((-inputValues[variable.ordinal()] / 2) * 1000);
  }

  @ParameterizedTest
  @EnumSource(SKTBVariable.class)
  void update(@Nonnull SKTBVariable variable) {
    SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);

    int[] rsceValues = RANDOM.ints(RsceVariable.values().length, 0, 1000).toArray();
    RsceEvent rsceEvent = new RsceEvent(this, rsceValues);
    control.update(rsceEvent);

    int[] sktbValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();
    control.accept(sktbValues);
    var rsceVariable = switch (variable) {
      case ROTATE -> RsceVariable.ROTATE;
      case FLEX -> RsceVariable.OPEN;
    };
    int angle = -(180 * rsceEvent.getValue(rsceVariable) / 100 - 90);
    Assertions.assertThat(control.velocity()).as(rsceEvent::toString).isEqualTo(((angle - sktbValues[variable.ordinal()]) / 2) * 1000);
  }
}