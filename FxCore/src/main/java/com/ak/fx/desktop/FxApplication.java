package com.ak.fx.desktop;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.fx.stage.ScreenResolutionMonitor;
import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.util.OSDockImage;
import com.ak.logging.LogPathBuilder;
import com.ak.storage.Storage;
import com.ak.util.OS;
import com.ak.util.PropertiesSupport;
import com.ak.util.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.MessageSourceResourceBundle;

public final class FxApplication extends Application {
  private static final String APP_PARAMETER_CONTEXT = "context";
  private static final String SCENE_XML = "scene.fxml";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_VERSION = "application.version";
  private static final String KEY_APPLICATION_IMAGE = "application.image";
  private static final String LOGGING_PROPERTIES = "logging.properties";
  private static final String KEY_PROPERTIES = "keys.properties";

  @Nonnull
  private ConfigurableApplicationContext context = new GenericApplicationContext();

  static {
    initLogger();
  }

  public static void main(String[] args) {
    launch(FxApplication.class, args);
  }

  @Override
  public void init() {
    Logger.getLogger(getClass().getName()).log(Level.INFO, getParameters().getRaw().toString());
    context = new FxClassPathXmlApplicationContext(getParameters().getNamed().get(APP_PARAMETER_CONTEXT));
  }

  @Override
  public void start(@Nonnull Stage stage) throws Exception {
    try {
      URL resource = getClass().getResource(SCENE_XML);
      if (!context.getApplicationName().isEmpty()) {
        resource = Optional.ofNullable(
            getClass().getResource(String.format("%s/%s", context.getApplicationName(), SCENE_XML))).orElse(resource);
      }
      FXMLLoader loader = new FXMLLoader(resource, new MessageSourceResourceBundle(
          BeanFactoryUtils.beanOfType(context, MessageSource.class), Locale.getDefault()));
      loader.setControllerFactory(clazz -> BeanFactoryUtils.beanOfType(context, clazz));
      stage.setScene(loader.load());
      String applicationFullName = getApplicationFullName(loader.getResources().getString(KEY_APPLICATION_TITLE));
      stage.setTitle(applicationFullName);
      if (!PropertiesSupport.OUT_CONVERTER_PATH.check()) {
        PropertiesSupport.OUT_CONVERTER_PATH.set(applicationFullName);
      }
      OSDockImage.valueOf(OS.get().name()).setIconImage(stage,
          getClass().getResource(loader.getResources().getString(KEY_APPLICATION_IMAGE)));

      Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass());
      stage.setOnCloseRequest(event -> stageStorage.save(stage));
      stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
      stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        if (KeyCombination.keyCombination("Ctrl+Shortcut+F").match(event)) {
          Platform.runLater(() -> {
            stage.setFullScreen(!stage.isFullScreen());
            stage.setResizable(false);
            stage.setResizable(true);
          });
        }
      });
      stage.show();
      stageStorage.update(stage);
      ScreenResolutionMonitor.setStage(stage);
    }
    catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public void stop() throws Exception {
    try {
      super.stop();
    }
    finally {
      context.close();
      Platform.exit();
    }
  }

  private static void initLogger() {
    try (InputStream in = FxApplication.class.getResourceAsStream(KEY_PROPERTIES)) {
      Properties keys = new Properties();
      keys.load(in);
      Path path = new LogPathBuilder().addPath(getApplicationFullName(keys.getProperty(KEY_APPLICATION_TITLE, Strings.EMPTY))).
          fileName(LOGGING_PROPERTIES).build().getPath();
      if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
        Files.copy(FxApplication.class.getResourceAsStream(LOGGING_PROPERTIES), path);
      }
      System.setProperty("java.util.logging.config.file", path.toAbsolutePath().toString());
      Logger.getLogger(FxApplication.class.getName()).log(Level.INFO, path.toAbsolutePath().toString());
    }
    catch (Exception e) {
      Logger.getGlobal().log(Level.WARNING, e.getMessage(), e);
    }
  }

  private static String getApplicationFullName(@Nonnull String name) {
    return name.replaceFirst(KEY_APPLICATION_VERSION, Strings.EMPTY).trim();
  }
}