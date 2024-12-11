package com.ak.spring;

import com.ak.appliance.kleiber.comm.converter.KleiberVariable;
import com.ak.appliance.kleiber.comm.interceptor.KleiberBytesInterceptor;
import com.ak.appliance.nmi.comm.converter.NmiVariable;
import com.ak.appliance.nmis.comm.bytes.NmisRequest;
import com.ak.appliance.nmis.comm.bytes.NmisResponseFrame;
import com.ak.appliance.nmis.comm.converter.NmisConverter;
import com.ak.appliance.nmis.comm.converter.NmisVariable;
import com.ak.appliance.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.appliance.prv.comm.converter.PrvVariable;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.FloatToIntegerConverter;
import com.ak.comm.converter.StringToIntegerConverter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.interceptor.simple.StringBytesInterceptor;
import com.ak.fx.FxApplication;
import com.ak.logging.LocalFileHandler;
import javafx.fxml.FXMLLoader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.ak.fx.desktop", "com.ak.appliance",
    "com.ak.comm.interceptor", "com.ak.comm.converter"
})
public class SpringFxApplication extends FxApplication {
  private ConfigurableApplicationContext applicationContext = new StaticApplicationContext();

  public static void main(String[] args) {
    launch(SpringFxApplication.class, args);
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
  protected List<FXMLLoader> getFXMLLoader(ResourceBundle resourceBundle) {
    String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
    if (profiles.length == 0) {
      profiles = applicationContext.getEnvironment().getDefaultProfiles();
    }
    var defaultFxmlLoader = super.getFXMLLoader(resourceBundle).getFirst();
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
  @Profile("NMI3Acc2Rheo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
  @Profile("kleiber-myo")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptorKleiberMyo() {
    return new KleiberBytesInterceptor();
  }

  @Bean
  @Profile("nmis")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<NmisRequest, NmisResponseFrame> bytesInterceptorNmis() {
    return new NmisBytesInterceptor();
  }

  @Bean
  @Profile("nmis")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<NmisResponseFrame, NmisVariable> converterNmis() {
    return new NmisConverter();
  }
}