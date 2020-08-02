package com.ak.fx.desktop.aper;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.aper.Aper2OutVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.comm.converter.aper.AperOutVariable;
import com.ak.comm.converter.aper.calibration.AperCalibrationVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
class AperConfiguration {
  @Bean
  @Profile({"aper", "aper-calibration", "aper2"})
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static BytesInterceptor<BufferFrame, BufferFrame> bytesInterceptor() {
    return new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_460800, 25);
  }

  @Bean
  @Profile("aper")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperOutVariable> converterDefault() {
    return converter(AperOutVariable.class);
  }

  @Bean
  @Profile("aper-calibration")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, AperCalibrationVariable> converterCalibration() {
    return converter(AperCalibrationVariable.class);
  }

  @Bean
  @Profile("aper2")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, Aper2OutVariable> converter2() {
    return new LinkedConverter<>(converterDefault(), Aper2OutVariable.class);
  }

  private static <O extends Enum<O> & DependentVariable<AperInVariable, O>> Converter<BufferFrame, O> converter(Class<O> aClass) {
    return new LinkedConverter<>(new ToIntegerConverter<>(AperInVariable.class, 1000), aClass);
  }
}
