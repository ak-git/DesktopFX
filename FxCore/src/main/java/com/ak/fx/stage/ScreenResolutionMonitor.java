package com.ak.fx.stage;

import java.awt.Toolkit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.Timer;

import com.ak.util.UIConstants;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Screen;

@Immutable
@ThreadSafe
public enum ScreenResolutionMonitor {
  INSTANCE;

  private final AtomicReference<Scene> sceneReference = new AtomicReference<>();
  private final IntegerProperty dpi = new SimpleIntegerProperty(Toolkit.getDefaultToolkit().getScreenResolution());

  ScreenResolutionMonitor() {
    if (Platform.isFxApplicationThread()) {
      dpi.setValue(Screen.getPrimary().getDpi());
    }
    log();
    Timer timer = new Timer((int) UIConstants.UI_DELAY.toMillis(), e ->
        Optional.ofNullable(sceneReference.get()).flatMap(scene ->
            Optional.ofNullable(scene.getWindow())).ifPresent(window -> {
          ObservableList<Screen> screens = Screen.getScreensForRectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight());
          Screen screen = Screen.getPrimary();
          if (!screens.isEmpty()) {
            screen = screens.get(0);
          }
          dpi.setValue(screen.getDpi());
        })
    );
    timer.start();
    dpi.addListener((observable, oldValue, newValue) -> log());
  }

  public double getDpi() {
    return dpi.get();
  }

  public ObservableValue<Number> dpi(@Nonnull Supplier<Scene> sceneSupplier) {
    sceneReference.set(sceneSupplier.get());
    return ReadOnlyIntegerProperty.readOnlyIntegerProperty(dpi);
  }

  private void log() {
    Logger.getLogger(getClass().getName()).log(Level.CONFIG, () -> "Screen resolution is %d dpi".formatted(dpi.get()));
  }
}
