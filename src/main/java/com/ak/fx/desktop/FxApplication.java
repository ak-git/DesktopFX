package com.ak.fx.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.converter.Refreshable;
import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.storage.Storage;
import com.ak.fx.util.OSDockImage;
import com.ak.logging.LoggingBuilder;
import com.ak.util.Extension;
import com.ak.util.OS;
import com.ak.util.PropertiesSupport;
import com.ak.util.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import static com.ak.util.Strings.POINT;

public final class FxApplication extends Application {
  private static final String SCENE_XML = "scene.fxml";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_VERSION = "application.version";
  private static final String KEY_APPLICATION_IMAGE = "application.image";
  private static final String KEY_PROPERTIES = "keys";
  private final List<AbstractApplicationContext> contexts = Arrays.stream(PropertiesSupport.CONTEXT.split())
      .map(s -> new FxClassPathXmlApplicationContext(FxApplication.class, s))
      .collect(Collectors.toUnmodifiableList());

  static {
    initLogger();
  }

  public static void main(String[] args) {
    launch(FxApplication.class);
  }

  @Override
  public void init() {
    Logger.getLogger(getClass().getName()).log(Level.INFO, PropertiesSupport.CONTEXT::value);
  }

  @Override
  public void start(@Nonnull Stage mainStage) throws IOException {
    List<FXMLLoader> fxmlLoaders = contexts.stream()
        .map(context -> Optional
            .ofNullable(getClass().getResource(String.join(POINT, context.getDisplayName(), SCENE_XML)))
            .orElse(getClass().getResource(SCENE_XML)))
        .map(url -> new FXMLLoader(url, ResourceBundle.getBundle(String.join(POINT, getClass().getPackageName(), KEY_PROPERTIES))))
        .collect(Collectors.toUnmodifiableList());

    List<Stage> stages = Stream.concat(
        Stream.of(mainStage),
        IntStream.range(1, fxmlLoaders.size()).mapToObj(i -> new Stage(StageStyle.DECORATED)))
        .collect(Collectors.toUnmodifiableList());

    ResourceBundle resourceBundle = fxmlLoaders.get(0).getResources();
    String applicationFullName = getApplicationFullName(
        resourceBundle.getString(KEY_APPLICATION_TITLE), resourceBundle.getString(KEY_APPLICATION_VERSION));
    if (!PropertiesSupport.OUT_CONVERTER_PATH.check()) {
      PropertiesSupport.OUT_CONVERTER_PATH.update(applicationFullName);
    }
    OSDockImage.valueOf(OS.get().name()).setIconImage(mainStage,
        getClass().getResource(resourceBundle.getString(KEY_APPLICATION_IMAGE)));

    for (int i = 0; i < fxmlLoaders.size(); i++) {
      ListableBeanFactory context = contexts.get(i);
      fxmlLoaders.get(i).setControllerFactory(clazz -> BeanFactoryUtils.beanOfType(context, clazz));
      Stage stage = stages.get(i);
      stage.setScene(fxmlLoaders.get(i).load());
      stage.setTitle(applicationFullName);

      Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass(), String.format("%d", i));
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
              contexts.forEach(c -> c.getBeansOfType(Refreshable.class).values().forEach(Refreshable::refresh)),
          KeyCode.SHORTCUT, KeyCode.N);
      stage.show();
      stageStorage.update(stage);
    }
  }

  @Override
  public void stop() throws Exception {
    try {
      contexts.forEach(ConfigurableApplicationContext::close);
      super.stop();
    }
    finally {
      Platform.exit();
    }
  }

  private static void initLogger() {
    try (InputStream in = FxApplication.class.getResourceAsStream(Extension.PROPERTIES.attachTo(KEY_PROPERTIES))) {
      Properties keys = new Properties();
      keys.load(in);
      Path path = LoggingBuilder.LOGGING.build(
          getApplicationFullName(keys.getProperty(KEY_APPLICATION_TITLE, Strings.EMPTY), keys.getProperty(KEY_APPLICATION_VERSION, Strings.EMPTY))
      ).getPath();
      if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
        PropertiesSupport.CACHE.update(Boolean.FALSE.toString());
        Files.copy(FxApplication.class.getResourceAsStream(LoggingBuilder.LOGGING.fileName()),
            path, StandardCopyOption.REPLACE_EXISTING);
      }
      System.setProperty("java.util.logging.config.file", path.toAbsolutePath().toString());
      Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, () -> path.toAbsolutePath().toString());
    }
    catch (Exception e) {
      Logger.getGlobal().log(Level.WARNING, e.getMessage(), e);
    }
  }

  @ParametersAreNonnullByDefault
  private static String getApplicationFullName(String title, String version) {
    return String.join(Strings.SPACE, title, version);
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