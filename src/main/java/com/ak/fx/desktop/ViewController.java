package com.ak.fx.desktop;

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

  default void zoom(double zoomFactor) {
    // works in subclasses
  }

  default void scroll(double deltaX) {
    // works in subclasses
  }
}
