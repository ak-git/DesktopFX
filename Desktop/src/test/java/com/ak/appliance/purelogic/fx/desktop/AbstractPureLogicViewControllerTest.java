package com.ak.appliance.purelogic.fx.desktop;

import com.ak.util.Builder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.IntStream;

abstract class AbstractPureLogicViewControllerTest<T extends AbstractPureLogicViewController> implements Builder<T> {
  @Test
  void testGet() {
    try (AbstractPureLogicViewController controller = build()) {
      controller.close();
      Assertions.assertThat(IntStream.range(0, 5 + 1 + 11 + 1).mapToDouble(_ -> controller.get().getMicrons()).toArray())
          .containsExactly(new double[] {
                  0.0, 0.0, 0.0, 0.0, 0.0,
                  -45.0,
                  90.0, -90.0, 90.0, -90.0, 90.0, -90.0, 90.0, -90.0, 90.0, -90.0, 90.0,
                  -45.0
              },
              Assertions.withPrecision(0.1));
      controller.escape();
      Assertions.assertThat(IntStream.range(0, 6 + 12 + 6).mapToDouble(_ -> controller.get().getMicrons()).toArray())
          .containsExactly(new double[] {
                  -7.5, -7.5, -7.5, -7.5, -7.5, -7.5,
                  7.5, 7.5, 7.5, 7.5, 7.5, 7.5, 7.5, 7.5, 7.5, 7.5, 7.5, 7.5,
                  -7.5, -7.5, -7.5, -7.5, -7.5, -7.5
              },
              Assertions.withPrecision(0.1)
          );
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void upThanRight() {
    try (AbstractPureLogicViewController controller = build()) {
      controller.close();

      controller.left();
      controller.up();
      Assertions.assertThat(IntStream.range(0, 2)
              .mapToDouble(_ -> controller.get().getMicrons()).toArray())
          .containsExactly(new double[] {450, 0}, Assertions.withPrecision(0.1));
      controller.right();
      controller.up();
      Assertions.assertThat(IntStream.range(0, 1 + 5 + 1 + 11 + 1)
              .mapToDouble(_ -> controller.get().getMicrons()).toArray())
          .containsExactly(new double[] {
              180.0,
              0.0, 0.0, 0.0, 0.0, 0.0,
              -45.0,
              90.0, -90.0, 90.0, -90.0, 90.0, -90.0, 90.0, -90.0, 90.0, -90.0, 90.0,
              -45.0
          }, Assertions.withPrecision(0.1));
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void downThenLeft() {
    try (AbstractPureLogicViewController controller = build()) {
      controller.close();

      controller.left();
      controller.down();
      Assertions.assertThat(IntStream.range(0, 2)
              .mapToDouble(_ -> controller.get().getMicrons()).toArray())
          .containsExactly(new double[] {-450.0, 0.0}, Assertions.withPrecision(0.1));
      controller.left();
      controller.down();
      Assertions.assertThat(IntStream.range(0, 2)
              .mapToDouble(_ -> controller.get().getMicrons()).toArray())
          .containsExactly(new double[] {-450.0, 0.0}, Assertions.withPrecision(0.1));
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void tryRefresh() {
    Assertions.assertThatNullPointerException().isThrownBy(() -> {
      try (AbstractPureLogicViewController controller = build()) {
        controller.refresh(true);
      }
    });
  }
}
