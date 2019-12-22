package com.ak.fx.desktop;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.util.OSDockImage;
import com.ak.logging.LoggingBuilder;
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
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public final class FxApplication extends Application {
  private static final String APP_PARAMETER_CONTEXT = "context";
  private static final String SCENE_XML = "scene.fxml";
  private static final String KEY_APPLICATION_TITLE = "application.title";
  private static final String KEY_APPLICATION_VERSION = "application.version";
  private static final String KEY_APPLICATION_IMAGE = "application.image";
  private static final String KEY_PROPERTIES = "keys";

  @Nonnull
  private ConfigurableApplicationContext context = new GenericApplicationContext();

  static {
    initLogger();
  }

  public static void main(String[] args) {
    String[] arguments = Arrays.copyOf(args, args.length);
    Options options = new Options();
    options.addOption(Option.builder().desc("Device context").longOpt(APP_PARAMETER_CONTEXT).type(String.class).hasArg().build());
    try {
      launch(FxApplication.class, new DefaultParser().parse(options, arguments).getOptionValue(APP_PARAMETER_CONTEXT, Strings.EMPTY));
    }
    catch (ParseException e) {
      new HelpFormatter().printHelp("FxDesktop", options);
      Platform.exit();
    }
  }

  @Override
  public void init() {
    Logger.getLogger(getClass().getName()).log(Level.INFO, () -> getParameters().getRaw().toString());
    context = new FxClassPathXmlApplicationContext(getParameters().getRaw().get(0));
  }

  @Override
  public void start(@Nonnull Stage stage) throws Exception {
    URL resource = getClass().getResource(SCENE_XML);
    if (!context.getApplicationName().isEmpty()) {
      resource = Optional.ofNullable(
          getClass().getResource(String.format("%s/%s", context.getApplicationName(), SCENE_XML))).orElse(resource);
    }
    FXMLLoader loader = new FXMLLoader(resource, ResourceBundle.getBundle(String.format("%s.%s", getClass().getPackageName(), KEY_PROPERTIES)));
    loader.setControllerFactory(clazz -> BeanFactoryUtils.beanOfType(context, clazz));
    stage.setScene(loader.load());
    String applicationFullName = getApplicationFullName(loader.getResources().getString(KEY_APPLICATION_TITLE), loader.getResources().getString(KEY_APPLICATION_VERSION));
    stage.setTitle(applicationFullName);
    if (!PropertiesSupport.OUT_CONVERTER_PATH.check()) {
      PropertiesSupport.OUT_CONVERTER_PATH.update(applicationFullName);
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
    try (InputStream in = FxApplication.class.getResourceAsStream(PropertiesSupport.addExtension(KEY_PROPERTIES))) {
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

  private static String getApplicationFullName(@Nonnull String title, @Nonnull String version) {
    return String.format("%s %s", title, version);
  }
}