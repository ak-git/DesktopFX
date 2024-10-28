package com.ak.appliance.sktbpr.fx.desktop;

import com.ak.appliance.nmisr.fx.desktop.RsceEvent;
import com.ak.appliance.rsce.comm.converter.RsceVariable;
import com.ak.appliance.sktbpr.comm.converter.SKTBVariable;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SKTBAngleVelocityControlTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @Test
  void accept() {
    int[] inputValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();
    assertThat(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> {
          SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);
          control.accept(inputValues);
          return control.velocity();
        })
    ).containsExactly(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> (-inputValues[variable.ordinal()] / 2) * 1000)
            .boxed().toArray(Integer[]::new)
    );
  }

  @Test
  void decrement() {
    int[] inputValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();
    assertThat(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> {
          SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);

          int maxCalls = RANDOM.nextInt(1, 10);
          for (int i = 0; i < maxCalls; i++) {
            control.decrement();
          }

          control.accept(inputValues);
          control.escape();
          assertThat(control.velocity()).as(() -> "control.decrement() calls count = %d".formatted(maxCalls))
              .isEqualTo(((-10 * maxCalls - inputValues[variable.ordinal()]) / 2) * 1000);

          control.accept(inputValues);
          return control.velocity();
        })
    ).containsExactly(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> (-inputValues[variable.ordinal()] / 2) * 1000)
            .boxed().toArray(Integer[]::new)
    );
  }

  @Test
  void increment() {
    int[] inputValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();
    assertThat(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> {
          SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);

          int maxCalls = RANDOM.nextInt(1, 10);
          for (int i = 0; i < maxCalls; i++) {
            control.increment();
          }

          control.accept(inputValues);
          control.escape();
          assertThat(control.velocity()).as(() -> "control.increment() calls count = %d".formatted(maxCalls))
              .isEqualTo(((10 * maxCalls - inputValues[variable.ordinal()]) / 2) * 1000);

          control.accept(inputValues);
          return control.velocity();
        })
    ).containsExactly(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> (-inputValues[variable.ordinal()] / 2) * 1000)
            .boxed().toArray(Integer[]::new)
    );
  }

  @Test
  void update() {
    int[] rsceValues = RANDOM.ints(RsceVariable.values().length, 0, 1000).toArray();
    RsceEvent rsceEvent = new RsceEvent(this, rsceValues);
    int[] sktbValues = RANDOM.ints(SKTBVariable.values().length, 0, 1000).toArray();

    assertThat(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> {
          SKTBAngleVelocityControl control = new SKTBAngleVelocityControl(variable);
          control.update(rsceEvent);
          control.accept(sktbValues);
          return control.velocity();
        })
    ).as(rsceEvent::toString).containsExactly(
        Stream.of(SKTBVariable.values()).mapToInt(variable -> {
              var rsceVariable = switch (variable) {
                case ROTATE -> RsceVariable.ROTATE;
                case FLEX -> RsceVariable.OPEN;
              };
              int angle = -(180 * rsceEvent.getValue(rsceVariable) / 100 - 90);
              return ((angle - sktbValues[variable.ordinal()]) / 2) * 1000;
            })
            .boxed().toArray(Integer[]::new)
    );
  }
}