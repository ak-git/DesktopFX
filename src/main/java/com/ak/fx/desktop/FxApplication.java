package com.ak.fx.desktop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.storage.Storage;
import com.ak.fx.util.OSDockImage;
import com.ak.util.OS;
import com.ak.util.Strings;
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
    var resourceBundle = ResourceBundle.getBundle(String.join(".", getClass().getPackageName(), KEY_PROPERTIES));
    List<FXMLLoader> fxmlLoaders = getFXMLLoader(resourceBundle);
    OSDockImage.valueOf(OS.get().name()).setIconImage(mainStage,
        Objects.requireNonNull(getClass().getResource(resourceBundle.getString(KEY_APPLICATION_IMAGE)))
    );

    var root = new SplitPane();
    root.setOrientation(Orientation.VERTICAL);
    for (FXMLLoader fxmlLoader : fxmlLoaders) {
      root.getItems().add(fxmlLoader.load());
    }

    var stage = new Stage(StageStyle.DECORATED);
    stage.setScene(new Scene(root, 1024, 768));
    stage.setTitle(resourceBundle.getString(KEY_APPLICATION_TITLE));
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    stage.getScene().setOnZoom(this::zoom);
    stage.getScene().setOnScroll(this::scroll);

    addEventHandler(stage, () ->
            Platform.runLater(() -> {
              stage.setFullScreen(!stage.isFullScreen());
              stage.setResizable(false);
              stage.setResizable(true);
            }),
        KeyCode.CONTROL, KeyCode.SHORTCUT, KeyCode.F);
    addEventHandler(stage, () -> refresh(false), KeyCode.N);
    addEventHandler(stage, () -> refresh(true), KeyCode.S);
    addEventHandler(stage, this::up, KeyCode.UP);
    addEventHandler(stage, this::down, KeyCode.DOWN);
    addEventHandler(stage, this::escape, KeyCode.ESCAPE);

    Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass(), Strings.EMPTY);
    stage.setOnCloseRequest(event -> stageStorage.save(stage));
    stage.getScene().addPostLayoutPulseListener(new Runnable() {
      @Override
      public void run() {
        stageStorage.update(stage);
        stage.getScene().removePostLayoutPulseListener(this);
      }
    });
    stage.show();
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
          var combination = String.join("+", Arrays.stream(codes).map(KeyCode::getName).toArray(String[]::new));
          if (KeyCombination.keyCombination(combination).match(event)) {
            runnable.run();
          }
        }
    );
  }
}