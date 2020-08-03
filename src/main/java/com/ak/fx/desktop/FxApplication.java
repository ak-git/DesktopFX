package com.ak.fx.desktop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Refreshable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.aper.Aper2OutVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.comm.converter.aper.AperOutVariable;
import com.ak.comm.converter.aper.calibration.AperCalibrationVariable;
import com.ak.comm.converter.rcm.RcmCalibrationVariable;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmOutVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.FixedFrameBytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

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

  @Bean
  @Profile("default")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptor() {
    return new FixedFrameBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 224);
  }

  @Bean
  @Profile("default")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, ADCVariable> converter() {
    return new ToIntegerConverter<>(ADCVariable.class, 1000);
  }

  @Bean
  @Profile({"aper", "aper-calibration", "aper2"})
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptorAper() {
    return new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 25);
  }

  @Bean
  @Profile("aper")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperOutVariable> converterAper() {
    return converterAper(AperOutVariable.class);
  }

  @Bean
  @Profile("aper-calibration")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperCalibrationVariable> converterAperCalibration() {
    return converterAper(AperCalibrationVariable.class);
  }

  @Bean
  @Profile("aper2")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, Aper2OutVariable> converterAper2() {
    return new LinkedConverter<>(converterAper(), Aper2OutVariable.class);
  }

  private static <O extends Enum<O> & DependentVariable<AperInVariable, O>> Converter<BufferFrame, O> converterAper(Class<O> aClass) {
    return new LinkedConverter<>(new ToIntegerConverter<>(AperInVariable.class, 1000), aClass);
  }

  @Bean
  @Profile("rcm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, RcmOutVariable> converterRcm() {
    return new LinkedConverter<>(new RcmConverter(), RcmOutVariable.class);
  }

  @Bean
  @Profile("rcm-calibration")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, RcmCalibrationVariable> converterRcmCalibration() {
    return new LinkedConverter<>(new RcmConverter(), RcmCalibrationVariable.class);
  }
}