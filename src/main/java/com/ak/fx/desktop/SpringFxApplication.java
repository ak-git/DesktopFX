package com.ak.fx.desktop;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.nmis.NmisRequest;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.bytes.suntech.NIBPRequest;
import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.Refreshable;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.aper.AperCalibrationCurrent1Variable;
import com.ak.comm.converter.aper.AperStage1Variable;
import com.ak.comm.converter.aper.AperStage2UnitsVariable;
import com.ak.comm.converter.aper.AperStage3Current2NIBPVariable;
import com.ak.comm.converter.aper.AperStage3Variable;
import com.ak.comm.converter.aper.AperStage4Current1Variable;
import com.ak.comm.converter.aper.AperStage4Current2Variable;
import com.ak.comm.converter.aper.AperStage5Current1Variable;
import com.ak.comm.converter.aper.AperStage6Current1Variable7;
import com.ak.comm.converter.rcm.RcmCalibrationVariable;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmOutVariable;
import com.ak.comm.converter.rsce.RsceConverter;
import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.converter.suntech.NIBPConverter;
import com.ak.comm.converter.suntech.NIBPVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.nmisr.NmisRsceBytesInterceptor;
import com.ak.comm.interceptor.simple.FixedFrameBytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.interceptor.suntech.NIBPBytesInterceptor;
import com.ak.logging.LocalFileHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ak.fx.desktop", "com.ak.comm.interceptor.nmis", "com.ak.comm.converter.nmis"})
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
  public void refresh() {
    super.refresh();
    applicationContext.getBeansOfType(Refreshable.class).values().forEach(Refreshable::refresh);
  }

  @Override
  List<FXMLLoader> getFXMLLoader(@Nonnull ResourceBundle resourceBundle) {
    String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
    if (profiles.length == 0) {
      profiles = applicationContext.getEnvironment().getDefaultProfiles();
    }
    FXMLLoader defaultFxmlLoader = super.getFXMLLoader(resourceBundle).get(0);
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
        .collect(Collectors.toUnmodifiableList());
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
  @Profile("nmis-rsce")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<NmisRequest, RsceCommandFrame> bytesInterceptorNmisRsce() {
    return new NmisRsceBytesInterceptor();
  }

  @Bean
  @Profile("nmis-rsce")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<RsceCommandFrame, RsceVariable> converterNmisRsce() {
    return new RsceConverter();
  }

  @Bean
  @Profile({"aper2-nibp", "aper1-myo", "aper2-ecg", "aper1-R4", "aper1-2Rho-7mm", "aper1-calibration"})
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
  @Profile("suntech")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<NIBPRequest, NIBPResponse> bytesInterceptorNIBP() {
    return new NIBPBytesInterceptor();
  }

  @Bean
  @Profile("suntech")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<NIBPResponse, NIBPVariable> converterNIBP() {
    return new NIBPConverter();
  }

  @Bean
  @Profile("aper1-myo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static LinkedConverter<BufferFrame, AperStage3Variable, AperStage4Current1Variable> converterAper1Myo() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Variable.class).chainInstance(AperStage4Current1Variable.class);
  }

  @Bean
  @Profile("aper1-R4")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static LinkedConverter<BufferFrame, AperStage4Current1Variable, AperStage5Current1Variable> converterAper1R4() {
    return converterAper1Myo().chainInstance(AperStage5Current1Variable.class);
  }

  @Bean
  @Profile("aper1-2Rho-7mm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperStage6Current1Variable7> converterAper1To2Rho() {
    return converterAper1R4().chainInstance(AperStage6Current1Variable7.class);
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