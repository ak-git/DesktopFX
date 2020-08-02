package com.ak.fx.desktop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.converter.Refreshable;
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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FxApplication extends Application {
  private static final String KEY_PROPERTIES = "keys";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";
  private ConfigurableApplicationContext applicationContext;

  public static void main(@Nonnull String[] args) {
    Application.launch(FxApplication.class, args);
  }

  @Override
  public void init() {
    applicationContext = new SpringApplicationBuilder(FxApplication.class).headless(false).run();
  }

  @Override
  public void start(@Nonnull Stage stage) throws IOException {
    String profile = Arrays.stream(applicationContext.getEnvironment().getActiveProfiles()).findFirst().orElse("default");
    ResourceBundle resourceBundle = ResourceBundle.getBundle(String.join(".", getClass().getPackageName(), KEY_PROPERTIES));
    FXMLLoader fxmlLoader = new FXMLLoader(
        Optional
            .ofNullable(
                getClass().getResource(String.join(".", profile, "fxml"))
            )
            .orElse(
                getClass().getResource(String.join(".", "default", "fxml"))
            ),
        resourceBundle
    );
    fxmlLoader.setControllerFactory(applicationContext::getBean);
    stage.setScene(fxmlLoader.load());

    String applicationFullName = resourceBundle.getString(KEY_APPLICATION_TITLE);
    stage.setTitle(applicationFullName);
    if (!PropertiesSupport.OUT_CONVERTER_PATH.check()) {
      PropertiesSupport.OUT_CONVERTER_PATH.update(applicationFullName);
    }

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
    addEventHandler(stage, () ->
            applicationContext.getBeansOfType(Refreshable.class).values().forEach(Refreshable::refresh),
        KeyCode.SHORTCUT, KeyCode.N);
    stage.show();
    stageStorage.update(stage);
  }

  @Override
  public void stop() {
    applicationContext.close();
    Platform.exit();
  }

  @ParametersAreNonnullByDefault
  private static void addEventHandler(Stage stage, Runnable runnable, KeyCode... codes) {
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