package com.ak.fx.desktop;

import javafx.scene.input.ZoomEvent;

public interface ViewController {
  default void refresh() {
    // works in subclasses
  }

  default void zoom(ZoomEvent event) {
    event.consume();
  }
}
