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
      Assertions.assertThat(IntStream.range(0, 4)
          .map(_ -> controller.get().getMicrons()).toArray()).containsExactly(-90, 90, -90, 90);
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void upThanRight() {
    try (AbstractPureLogicViewController controller = build()) {
      controller.close();

      controller.escape();
      controller.up();
      Assertions.assertThat(IntStream.range(0, 2)
          .map(_ -> controller.get().getMicrons()).toArray()).containsExactly(90, 0);
      controller.right();
      controller.up();
      Assertions.assertThat(IntStream.range(0, 4)
          .map(_ -> controller.get().getMicrons()).toArray()).containsExactly(90, 90, -90, 90);
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void downThenLeft() {
    try (AbstractPureLogicViewController controller = build()) {
      controller.close();

      controller.escape();
      controller.down();
      Assertions.assertThat(IntStream.range(0, 2)
          .map(_ -> controller.get().getMicrons()).toArray()).containsExactly(-90, 0);
      controller.left();
      controller.down();
      Assertions.assertThat(IntStream.range(0, 4)
          .map(_ -> controller.get().getMicrons()).toArray()).containsExactly(90, -90, -90, 90);
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
