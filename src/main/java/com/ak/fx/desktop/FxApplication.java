package com.ak.fx.desktop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FxApplication extends Application {
  private static final String KEY_PROPERTIES = "keys";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";

  @Override
  @OverridingMethodsMustInvokeSuper
  public void start(@Nonnull Stage mainStage) throws IOException {
    ResourceBundle resourceBundle = ResourceBundle.getBundle(String.join(".", getClass().getPackageName(), KEY_PROPERTIES));
    List<FXMLLoader> fxmlLoaders = getFXMLLoader(resourceBundle);
    OSDockImage.valueOf(OS.get().name()).setIconImage(mainStage,
        getClass().getResource(resourceBundle.getString(KEY_APPLICATION_IMAGE))
    );

    List<Stage> stages = Stream
        .concat(
            Stream.of(mainStage),
            IntStream.range(1, fxmlLoaders.size()).mapToObj(i -> new Stage(StageStyle.DECORATED))
        )
        .collect(Collectors.toUnmodifiableList());

    for (int i = 0; i < stages.size(); i++) {
      Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass(), String.format("%d", i));
      Stage stage = stages.get(i);
      stage.setScene(fxmlLoaders.get(i).load());
      stage.setTitle(resourceBundle.getString(KEY_APPLICATION_TITLE));
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
  static void addEventHandler(Stage stage, Runnable runnable, KeyCode... codes) {
    stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (isMatchEvent(event, codes)) {
        runnable.run();
      }
    });
  }

  @ParametersAreNonnullByDefault
  public static boolean isMatchEvent(KeyEvent event, KeyCode... codes) {
    return KeyCombination.keyCombination(
        String.join("+", Arrays.stream(codes).map(KeyCode::getName).toArray(String[]::new))).match(event);
  }
}