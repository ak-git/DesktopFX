package com.ak.appliance.purelogic.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.IntStream;

class PureLogicViewControllerTest {
  @Test
  void testGet() {
    try (PureLogicViewController controller = new PureLogicViewController()) {
      controller.close();
      Assertions.assertThat(IntStream.range(0, 4)
          .map(ignore -> controller.get().getMicrons()).toArray()).containsExactly(-150, 150, -150, 150);
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void upThanRight() {
    try (PureLogicViewController controller = new PureLogicViewController()) {
      controller.close();

      controller.escape();
      controller.up();
      Assertions.assertThat(IntStream.range(0, 2)
          .map(ignore -> controller.get().getMicrons()).toArray()).containsExactly(750, 0);
      controller.right();
      controller.up();
      Assertions.assertThat(IntStream.range(0, 4)
          .map(ignore -> controller.get().getMicrons()).toArray()).containsExactly(150, 750, -150, 150);
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void downThenLeft() {
    try (PureLogicViewController controller = new PureLogicViewController()) {
      controller.close();

      controller.escape();
      controller.down();
      Assertions.assertThat(IntStream.range(0, 2)
          .map(ignore -> controller.get().getMicrons()).toArray()).containsExactly(-750, 0);
      controller.left();
      controller.down();
      Assertions.assertThat(IntStream.range(0, 4)
          .map(ignore -> controller.get().getMicrons()).toArray()).containsExactly(150, -750, -150, 150);
    }
    catch (IOException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @Test
  void tryRefresh() {
    Assertions.assertThatNullPointerException().isThrownBy(() -> {
      try (PureLogicViewController controller = new PureLogicViewController()) {
        controller.refresh(true);
      }
    });
  }
}