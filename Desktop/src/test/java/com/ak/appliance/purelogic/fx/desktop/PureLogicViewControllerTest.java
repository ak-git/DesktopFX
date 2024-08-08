package com.ak.appliance.purelogic.fx.desktop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;

class PureLogicViewControllerTest {
  @ParameterizedTest
  @ValueSource(classes = {PureLogicViewController2F0.class, PureLogicViewController0F5.class})
  void testGet(Class<AbstractPureLogicViewController> controllerClass) {
    try (AbstractPureLogicViewController controller = (AbstractPureLogicViewController) controllerClass.getConstructors()[0].newInstance()) {
      controller.close();
      Assertions.assertThat(IntStream.range(0, 4)
          .map(ignore -> controller.get().getMicrons()).toArray()).containsExactly(-150, 150, -150, 150);
    }
    catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @ParameterizedTest
  @ValueSource(classes = {PureLogicViewController2F0.class, PureLogicViewController0F5.class})
  void upThanRight(Class<AbstractPureLogicViewController> controllerClass) {
    try (AbstractPureLogicViewController controller = (AbstractPureLogicViewController) controllerClass.getConstructors()[0].newInstance()) {
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
    catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @ParameterizedTest
  @ValueSource(classes = {PureLogicViewController2F0.class, PureLogicViewController0F5.class})
  void downThenLeft(Class<AbstractPureLogicViewController> controllerClass) {
    try (AbstractPureLogicViewController controller = (AbstractPureLogicViewController) controllerClass.getConstructors()[0].newInstance()) {
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
    catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      Assertions.fail(e.getMessage(), e);
    }
  }

  @ParameterizedTest
  @ValueSource(classes = {PureLogicViewController2F0.class, PureLogicViewController0F5.class})
  void tryRefresh(Class<AbstractPureLogicViewController> controllerClass) {
    Assertions.assertThatNullPointerException().isThrownBy(() -> {
      try (AbstractPureLogicViewController controller = (AbstractPureLogicViewController) controllerClass.getConstructors()[0].newInstance()) {
        controller.refresh(true);
      }
    });
  }
}