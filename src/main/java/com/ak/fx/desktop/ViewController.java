package com.ak.fx.desktop;

import javax.annotation.Nonnull;

import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

public interface ViewController {
  default void refresh(boolean force) {
    // works in subclasses
  }

  default void up() {
    // works in subclasses
  }

  default void down() {
    // works in subclasses
  }

  default void escape() {
    // works in subclasses
  }

  default void zoom(@Nonnull ZoomEvent event) {
    event.consume();
  }

  default void scroll(@Nonnull ScrollEvent event) {
    event.consume();
  }
}
