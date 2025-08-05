package com.ak.fx.stage;

import com.ak.util.UIConstants;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Screen;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum ScreenResolutionMonitor {
  ;

  private static final AtomicReference<@Nullable Scene> SCENE_REFERENCE = new AtomicReference<>();
  private static final IntegerProperty DPI = new SimpleIntegerProperty(Toolkit.getDefaultToolkit().getScreenResolution());

  static {
    if (Platform.isFxApplicationThread()) {
      DPI.setValue(Screen.getPrimary().getDpi());
    }
    log();
    var timer = new Timer((int) UIConstants.UI_DELAY.toMillis(), _ ->
        Optional.ofNullable(SCENE_REFERENCE.get()).flatMap(scene ->
            Optional.ofNullable(scene.getWindow())).ifPresent(window -> {
          ObservableList<Screen> screens = Screen.getScreensForRectangle(
              window.getX(), window.getY(), window.getWidth(), window.getHeight()
          );
          var screen = Screen.getPrimary();
          if (!screens.isEmpty()) {
            screen = screens.getFirst();
          }
          DPI.setValue(screen.getDpi());
        })
    );
    timer.start();
    DPI.addListener((_, _, _) -> log());
  }

  public static double getDpi() {
    return DPI.get();
  }

  public static ObservableValue<Number> dpi(Supplier<Scene> sceneSupplier) {
    SCENE_REFERENCE.set(sceneSupplier.get());
    return ReadOnlyIntegerProperty.readOnlyIntegerProperty(DPI);
  }

  private static void log() {
    Logger.getLogger(ScreenResolutionMonitor.class.getName()).log(Level.CONFIG,
        () -> "Screen resolution is %d dpi".formatted(DPI.get()));
  }
}
