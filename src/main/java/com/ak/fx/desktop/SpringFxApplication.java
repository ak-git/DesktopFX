package com.ak.fx.desktop;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.*;
import com.ak.comm.converter.aper.*;
import com.ak.comm.converter.kleiber.KleiberVariable;
import com.ak.comm.converter.rcm.RcmCalibrationVariable;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmOutVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.FixedFrameBytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.interceptor.simple.StringBytesInterceptor;
import com.ak.logging.LocalFileHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.ak.fx.desktop",
    "com.ak.comm.interceptor", "com.ak.comm.converter"
})
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
  public void refresh(boolean force) {
    processEvent(viewController -> viewController.refresh(force));
  }

  @Override
  public void up() {
    processEvent(ViewController::up);
  }

  @Override
  public void down() {
    processEvent(ViewController::down);
  }

  @Override
  public void escape() {
    processEvent(ViewController::escape);
  }

  @Override
  public void zoom(double zoomFactor) {
    processEvent(viewController -> viewController.zoom(zoomFactor));
  }

  @Override
  public void scroll(double deltaX) {
    processEvent(viewController -> viewController.scroll(deltaX));
  }

  private void processEvent(Consumer<? super ViewController> action) {
    applicationContext.getBeansOfType(ViewController.class).values().parallelStream()
        .filter(viewController -> !SpringFxApplication.class.isAssignableFrom(viewController.getClass()))
        .forEach(action);
  }

  @Override
  @Nonnull
  List<FXMLLoader> getFXMLLoader(@Nonnull ResourceBundle resourceBundle) {
    String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
    if (profiles.length == 0) {
      profiles = applicationContext.getEnvironment().getDefaultProfiles();
    }
    var defaultFxmlLoader = super.getFXMLLoader(resourceBundle).get(0);
    List<FXMLLoader> fxmlLoaders = Arrays.stream(profiles)
        .map(profile -> getClass().getResource(String.join(".", profile, "fxml")))
        .map(fxml -> {
          if (fxml == null) {
            return defaultFxmlLoader;
          }
          else {
            return new FXMLLoader(fxml, resourceBundle);
          }
        })
        .toList();
    fxmlLoaders.forEach(fxmlLoader -> fxmlLoader.setControllerFactory(applicationContext::getBean));
    return fxmlLoaders;
  }

  @Override
  public void stop() {
    applicationContext.close();
    super.stop();
  }

  @Bean
  @Profile("loopback")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptor() {
    return new FixedFrameBytesInterceptor("loopback", BytesInterceptor.BaudRate.BR_460800, 1 + Integer.BYTES);
  }

  @Bean
  @Profile("loopback")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, ADCVariable> converter() {
    return new ToIntegerConverter<>(ADCVariable.class, 5);
  }

  @Bean
  @Profile("kleiber-myo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, KleiberVariable> converterKleiber() {
    return new FloatToIntegerConverter<>(KleiberVariable.class, 2000);
  }

  @Bean
  @Profile("prv")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<BufferFrame, String> bytesInterceptorPrv() {
    return new StringBytesInterceptor("prv");
  }

  @Bean
  @Profile("prv")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<String, ADCVariable> converterPrv() {
    return new StringToIntegerConverter<>(ADCVariable.class, 32);
  }

  @Bean
  @Profile({"aper2-nibp", "aper1-nibp", "aper1-myo", "aper2-ecg", "aper1-R2-6mm", "aper1-R2-7mm", "aper1-R2-8mm", "aper1-R2-10mm",
      "aper1-R1", "aper1-calibration"})
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptorAper() {
    return new RampBytesInterceptor("aper", BytesInterceptor.BaudRate.BR_460800, 25);
  }

  @Bean
  @Profile("aper2-nibp")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage3Current2NIBPVariable> converterAper2NIBP() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Current2NIBPVariable.class);
  }

  @Bean
  @Profile("aper1-nibp")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage3Current1NIBPVariable> converterAper1NIBP() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Current1NIBPVariable.class);
  }

  @Bean
  @Profile("aper1-myo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static LinkedConverter<BufferFrame, AperStage3Variable, AperStage4Current1Variable> converterAper1Myo() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Variable.class).chainInstance(AperStage4Current1Variable.class);
  }

  private static LinkedConverter<BufferFrame, AperStage4Current1Variable, AperStage5Current1Variable> converterAper1R() {
    return converterAper1Myo().chainInstance(AperStage5Current1Variable.class);
  }

  @Bean
  @Profile("aper1-R2-6mm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage6Current1Variable6mm> converterAper1R6() {
    return converterAper1R().chainInstance(AperStage6Current1Variable6mm.class);
  }

  @Bean
  @Profile("aper1-R2-7mm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage6Current1Variable7mm> converterAper1R7() {
    return converterAper1R().chainInstance(AperStage6Current1Variable7mm.class);
  }

  @Bean
  @Profile("aper1-R2-8mm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage6Current1Variable8mm> converterAper1R8() {
    return converterAper1R().chainInstance(AperStage6Current1Variable8mm.class);
  }

  @Bean
  @Profile("aper1-R2-10mm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage6Current1Variable10mm> converterAper1R10() {
    return converterAper1R().chainInstance(AperStage6Current1Variable10mm.class);
  }

  @Bean
  @Profile("aper1-R1")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static Converter<BufferFrame, AperStage6Current1Variable> converterAper1R1() {
    return converterAper1R().chainInstance(AperStage6Current1Variable.class);
  }

  @Bean
  @Profile("aper1-calibration")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperCalibrationCurrent1Variable> converterAper1Calibration() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000),
        AperCalibrationCurrent1Variable.class);
  }

  @Bean
  @Profile("aper2-ecg")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperStage4Current2Variable> converterAper2() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Variable.class).chainInstance(AperStage4Current2Variable.class);
  }

  @Bean
  @Profile("rcm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, RcmOutVariable> converterRcm() {
    return LinkedConverter.of(new RcmConverter(), RcmOutVariable.class);
  }

  @Bean
  @Profile("rcm-calibration")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, RcmCalibrationVariable> converterRcmCalibration() {
    return LinkedConverter.of(new RcmConverter(), RcmCalibrationVariable.class);
  }
}