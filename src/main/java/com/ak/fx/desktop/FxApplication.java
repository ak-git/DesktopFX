package com.ak.fx.desktop;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.storage.Storage;
import com.ak.fx.util.OSDockImage;
import com.ak.util.OS;
import com.ak.util.PropertiesSupport;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class FxApplication extends Application {
  private static final String KEY_PROPERTIES = "keys";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";

  @Override
  @OverridingMethodsMustInvokeSuper
  public void start(@Nonnull Stage stage) throws IOException {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(String.join(".", getClass().getPackageName(), KEY_PROPERTIES));
    stage.setScene(getFXMLLoader(resourceBundle).load());

    String applicationFullName = resourceBundle.getString(KEY_APPLICATION_TITLE);
    stage.setTitle(applicationFullName);
    PropertiesSupport.OUT_CONVERTER_PATH.update(applicationFullName);
    OSDockImage.valueOf(OS.get().name()).setIconImage(stage,
        getClass().getResource(resourceBundle.getString(KEY_APPLICATION_IMAGE)));

    Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass(), String.format("%d", 0));
    stage.setOnCloseRequest(event -> stageStorage.save(stage));
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    addEventHandler(stage, () ->
            Platform.runLater(() -> {
              stage.setFullScreen(!stage.isFullScreen());
              stage.setResizable(false);
              stage.setResizable(true);
            }),
        KeyCode.CONTROL, KeyCode.SHORTCUT, KeyCode.F);
    stage.show();
    stageStorage.update(stage);
  }

  @OverridingMethodsMustInvokeSuper
  FXMLLoader getFXMLLoader(@Nonnull ResourceBundle resourceBundle) {
    return new FXMLLoader(getClass().getResource(String.join(".", "default", "fxml")), resourceBundle);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void stop() {
    Platform.exit();
  }

  @ParametersAreNonnullByDefault
  static void addEventHandler(Stage stage, Runnable runnable, KeyCode... codes) {
    stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (isMatchEvent(event, codes)) {
        runnable.run();
      }
    });
  }

  @ParametersAreNonnullByDefault
  private static boolean isMatchEvent(KeyEvent event, KeyCode... codes) {
    return KeyCombination.keyCombination(
        String.join("+", Arrays.stream(codes).map(KeyCode::getName).toArray(String[]::new))).match(event);
  }
}