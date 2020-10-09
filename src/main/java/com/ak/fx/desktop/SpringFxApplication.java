package com.ak.fx.desktop;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

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
import com.ak.logging.LocalFileHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ak.comm", "com.ak.fx.desktop"})
public class SpringFxApplication extends FxApplication {
  private ConfigurableApplicationContext applicationContext;

  public static void main(@Nonnull String[] args) {
    Application.launch(SpringFxApplication.class, args);
  }

  @Override
  public void init() {
    System.setProperty(LocalFileHandler.class.getName(), "FxDesktop");
    applicationContext = new SpringApplicationBuilder(SpringFxApplication.class).headless(false).run();
  }

  @Override
  public void start(@Nonnull Stage stage) throws IOException {
    super.start(stage);
    addEventHandler(stage, () ->
            applicationContext.getBeansOfType(Refreshable.class).values().forEach(Refreshable::refresh),
        KeyCode.SHORTCUT, KeyCode.N);
  }

  @Override
  FXMLLoader getFXMLLoader(@Nonnull ResourceBundle resourceBundle) {
    String profile = Arrays.stream(applicationContext.getEnvironment().getActiveProfiles()).findFirst().orElse("default");
    FXMLLoader fxmlLoader = super.getFXMLLoader(resourceBundle);
    URL fxml = getClass().getResource(String.join(".", profile, "fxml"));
    if (fxml != null) {
      fxmlLoader = new FXMLLoader(fxml, resourceBundle);
    }
    fxmlLoader.setControllerFactory(applicationContext::getBean);
    return fxmlLoader;
  }

  @Override
  public void stop() {
    applicationContext.close();
    super.stop();
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