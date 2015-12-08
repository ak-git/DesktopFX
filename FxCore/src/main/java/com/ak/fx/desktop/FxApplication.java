package com.ak.fx.desktop;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.util.OSDockImage;
import com.ak.storage.Storage;
import com.ak.util.LocalFileIO;
import com.ak.util.OS;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.MessageSourceResourceBundle;

public final class FxApplication extends Application {
  private static final String FX_CONTEXT_XML = "fx-context.xml";
  private static final String SCENE_XML = "scene.fxml";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_IMAGE = "application.image";
  private static final String LOGGING_PROPERTIES = "logging.properties";
  private static final String KEY_PROPERTIES = "keys.properties";

  private final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
      Paths.get(getClass().getPackage().getName().replaceAll("\\.", "/"), FX_CONTEXT_XML).toString());

  static {
    initLogger();
  }

  public static void main(String[] args) {
    launch(FxApplication.class, args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(SCENE_XML), new MessageSourceResourceBundle(
          BeanFactoryUtils.beanOfType(context, MessageSource.class), Locale.getDefault()));
      loader.setControllerFactory(clazz -> BeanFactoryUtils.beanOfType(context, clazz));
      stage.setScene(new Scene(loader.load()));
      stage.setTitle(loader.getResources().getString(KEY_APPLICATION_TITLE));
      OSDockImage.valueOf(OS.get().name()).setIconImage(stage,
          getClass().getResource(loader.getResources().getString(KEY_APPLICATION_IMAGE)));

      Storage<Stage> stageStorage = OSStageStorage.valueOf(OS.get().name()).newInstance(getClass());
      stage.setOnCloseRequest(event -> stageStorage.save(stage));
      stageStorage.update(stage);
      stage.show();
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
    try {
      Properties keys = new Properties();
      keys.load(FxApplication.class.getResourceAsStream(KEY_PROPERTIES));
      Path path = new LocalFileIO.LogBuilder().addPath(keys.getProperty(KEY_APPLICATION_TITLE)).
          fileName(LOGGING_PROPERTIES).build().getPath();
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