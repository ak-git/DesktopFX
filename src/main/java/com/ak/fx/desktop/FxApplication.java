package com.ak.fx.desktop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.storage.Storage;
import com.ak.fx.util.OSDockImage;
import com.ak.util.OS;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FxApplication extends Application implements ViewController {
  private static final String KEY_PROPERTIES = "keys";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";

  @Override
  public final void start(@Nonnull Stage mainStage) throws IOException {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(String.join(".", getClass().getPackageName(), KEY_PROPERTIES));
    List<FXMLLoader> fxmlLoaders = getFXMLLoader(resourceBundle);
    OSDockImage.valueOf(OS.get().name()).setIconImage(mainStage,
        getClass().getResource(resourceBundle.getString(KEY_APPLICATION_IMAGE))
    );

    SplitPane root = new SplitPane();
    root.setOrientation(Orientation.VERTICAL);
    for (FXMLLoader fxmlLoader : fxmlLoaders) {
      root.getItems().add(fxmlLoader.load());
    }

    Stage stage = new Stage(StageStyle.DECORATED);
    stage.setScene(new Scene(root, 1024, 768));
    stage.setTitle(resourceBundle.getString(KEY_APPLICATION_TITLE));
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    stage.getScene().setOnZoom(this::zoom);
    addEventHandler(stage, () ->
            Platform.runLater(() -> {
              stage.setFullScreen(!stage.isFullScreen());
              stage.setResizable(false);
              stage.setResizable(true);
            }),
        KeyCode.CONTROL, KeyCode.SHORTCUT, KeyCode.F);
    addEventHandler(stage, this::refresh, KeyCode.SHORTCUT, KeyCode.N);
    stage.show();

    Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass(), "%d".formatted(0));
    stage.setOnCloseRequest(event -> stageStorage.save(stage));
    stageStorage.update(stage);
  }

  @OverridingMethodsMustInvokeSuper
  List<FXMLLoader> getFXMLLoader(@Nonnull ResourceBundle resourceBundle) {
    return Collections.singletonList(
        new FXMLLoader(getClass().getResource(String.join(".", "default", "fxml")), resourceBundle)
    );
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void stop() {
    Platform.exit();
  }

  @ParametersAreNonnullByDefault
  private static void addEventHandler(Stage stage, Runnable runnable, KeyCode... codes) {
    stage.addEventHandler(KeyEvent.KEY_RELEASED,
        event -> {
          String combination = String.join("+", Arrays.stream(codes).map(KeyCode::getName).toArray(String[]::new));
          if (KeyCombination.keyCombination(combination).match(event)) {
            runnable.run();
          }
        }
    );
  }
}