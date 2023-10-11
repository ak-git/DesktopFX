package com.ak.fx.desktop;

import org.springframework.context.ApplicationEvent;

import javax.annotation.Nonnull;

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

  default void left() {
    // works in subclasses
  }

  default void right() {
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

  final class RefreshEvent extends ApplicationEvent {
    private final boolean force;

    public RefreshEvent(@Nonnull Object source, boolean force) {
      super(source);
      this.force = force;
    }

    public boolean isForce() {
      return force;
    }
  }

  final class UpEvent extends ApplicationEvent {
    public UpEvent(@Nonnull Object source) {
      super(source);
    }
  }

  final class DownEvent extends ApplicationEvent {
    public DownEvent(@Nonnull Object source) {
      super(source);
    }
  }

  final class LeftEvent extends ApplicationEvent {
    public LeftEvent(@Nonnull Object source) {
      super(source);
    }
  }

  final class RightEvent extends ApplicationEvent {
    public RightEvent(@Nonnull Object source) {
      super(source);
    }
  }

  final class EscapeEvent extends ApplicationEvent {
    public EscapeEvent(@Nonnull Object source) {
      super(source);
    }
  }

  final class ZoomEvent extends ApplicationEvent {
    private final double zoomFactor;

    public ZoomEvent(@Nonnull Object source, double zoomFactor) {
      super(source);
      this.zoomFactor = zoomFactor;
    }

    public double getZoomFactor() {
      return zoomFactor;
    }
  }

  final class ScrollEvent extends ApplicationEvent {
    private final double deltaX;

    public ScrollEvent(@Nonnull Object source, double deltaX) {
      super(source);
      this.deltaX = deltaX;
    }

    public double getDeltaX() {
      return deltaX;
    }
  }
}
