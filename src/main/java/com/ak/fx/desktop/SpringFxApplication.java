package com.ak.fx.desktop;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.*;
import com.ak.comm.converter.aper.*;
import com.ak.comm.converter.briko.BrikoConverter;
import com.ak.comm.converter.briko.BrikoStage2Variable;
import com.ak.comm.converter.briko.BrikoStage3Variable;
import com.ak.comm.converter.briko.BrikoStage4Variable;
import com.ak.comm.converter.kleiber.KleiberVariable;
import com.ak.comm.converter.nmi.NmiVariable;
import com.ak.comm.converter.prv.PrvVariable;
import com.ak.comm.converter.rcm.RcmCalibrationVariable;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmOutVariable;
import com.ak.comm.interceptor.BytesInterceptor;
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
    applicationContext.publishEvent(new RefreshEvent(this, force));
  }

  @Override
  public void up() {
    applicationContext.publishEvent(new UpEvent(this));
  }

  @Override
  public void down() {
    applicationContext.publishEvent(new DownEvent(this));
  }

  @Override
  public void left() {
    applicationContext.publishEvent(new LeftEvent(this));
  }

  @Override
  public void right() {
    applicationContext.publishEvent(new RightEvent(this));
  }

  @Override
  public void escape() {
    applicationContext.publishEvent(new EscapeEvent(this));
  }

  @Override
  public void zoom(double zoomFactor) {
    applicationContext.publishEvent(new ZoomEvent(this, zoomFactor));
  }

  @Override
  public void scroll(double deltaX) {
    applicationContext.publishEvent(new ScrollEvent(this, deltaX));
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
  static Converter<String, PrvVariable> converterPrv() {
    return new StringToIntegerConverter<>(PrvVariable.class, 32);
  }

  @Bean
  @Profile({"aper2-nibp", "aper1-nibp", "aper1-myo", "aper2-emg1", "aper2-emg2",
      "aper1-R2-6mm", "aper1-R2-7mm", "aper1-R2-8mm", "aper1-R2-10mm", "aper1-R1", "aper1-calibration"})
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
  @Profile({"aper2-emg1", "aper2-emg2"})
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
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

  @Bean
  @Profile("NMI3Acc2Rheo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Primary
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptorNMI3Acc2Rheo() {
    return new RampBytesInterceptor("NMI3Acc2Rheo", BytesInterceptor.BaudRate.BR_921600, 1 + (3 + 2) * Integer.BYTES);
  }

  @Bean
  @Profile("NMI3Acc2Rheo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, NmiVariable> converterNMI3Acc2Rheo() {
    return new ToIntegerConverter<>(NmiVariable.class, 125);
  }

  @Bean
  @Profile("briko-black")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, BrikoStage4Variable> converterBriko() {
    return LinkedConverter.of(new BrikoConverter(), BrikoStage2Variable.class)
        .chainInstance(BrikoStage3Variable.class).chainInstance(BrikoStage4Variable.class);
  }
}