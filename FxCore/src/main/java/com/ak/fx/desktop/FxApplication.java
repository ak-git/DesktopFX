package com.ak.fx.desktop;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.ak.util.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.MessageSourceResourceBundle;

public final class FxApplication extends Application {
  private static final String APP_PARAMETER_CONTEXT = "context";
  private static final String CONTEXT_XML = "context.xml";
  private static final String SCENE_XML = "scene.fxml";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";
  private static final String LOGGING_PROPERTIES = "logging.properties";
  private static final String KEY_PROPERTIES = "keys.properties";

  @Nonnull
  private ConfigurableApplicationContext context = new GenericApplicationContext();
  @Nonnull
  private String contextName = Strings.EMPTY;

  static {
    initLogger();
  }

  public static void main(String[] args) {
    launch(FxApplication.class, args);
  }

  @Override
  public void init() {
    Logger.getLogger(getClass().getName()).log(Level.INFO, getParameters().getRaw().toString());

    Path path = Paths.get(getClass().getPackage().getName().replaceAll("\\.", "/"));
    contextName = Optional.ofNullable(getParameters().getNamed().get(APP_PARAMETER_CONTEXT)).orElse(Strings.EMPTY);
    if (contextName.isEmpty()) {
      path = path.resolve(CONTEXT_XML);
    }
    else {
      path = path.resolve(contextName).resolve(String.format("%s-%s", contextName, CONTEXT_XML));
    }
    context = new ClassPathXmlApplicationContext(path.toString());
  }

  @Override
  public void start(@Nonnull Stage stage) throws Exception {
    try {
      URL resource = getClass().getResource(SCENE_XML);
      if (!contextName.isEmpty()) {
        resource = Optional.ofNullable(getClass().getResource(String.format("%1$s/%1$s-%2$s", contextName, SCENE_XML))).orElse(resource);
      }
      FXMLLoader loader = new FXMLLoader(resource, new MessageSourceResourceBundle(
          BeanFactoryUtils.beanOfType(context, MessageSource.class), Locale.getDefault()));
      loader.setControllerFactory(clazz -> BeanFactoryUtils.beanOfType(context, clazz));
      stage.setScene(new Scene(loader.load()));
      stage.setTitle(loader.getResources().getString(KEY_APPLICATION_TITLE));
      OSDockImage.valueOf(OS.get().name()).setIconImage(stage,
          getClass().getResource(loader.getResources().getString(KEY_APPLICATION_IMAGE)));

      Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass());
      stage.setOnCloseRequest(event -> stageStorage.save(stage));
      stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
      stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        if (event.isShortcutDown() && event.isControlDown() && event.getCode() == KeyCode.F) {
          Platform.runLater(() -> stage.setFullScreen(!stage.isFullScreen()));
        }
      });
      stageStorage.update(stage);
      stage.show();
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
      Path path = new LogPathBuilder().addPath(keys.getProperty(KEY_APPLICATION_TITLE)).fileName(LOGGING_PROPERTIES).build().getPath();
      if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
        Files.copy(FxApplication.class.getResourceAsStream(LOGGING_PROPERTIES), path);
      }
      System.setProperty("java.util.logging.config.file", path.toAbsolutePath().toString());
    }
    catch (Exception e) {
      Logger.getGlobal().log(Level.WARNING, e.getMessage(), e);
    }
  }
}