package com.ak.fx.desktop.rcm;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.rcm.RcmConverter;
import com.ak.comm.converter.rcm.RcmOutVariable;
import com.ak.comm.converter.rcm.calibration.RcmCalibrationVariable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
class RcmConfiguration {
  @Bean
  @Profile("rcm")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, RcmOutVariable> converterDefault() {
    return new LinkedConverter<>(new RcmConverter(), RcmOutVariable.class);
  }

  @Bean
  @Profile("rcm-calibration")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  static Converter<BufferFrame, RcmCalibrationVariable> converterCalibration() {
    return new LinkedConverter<>(new RcmConverter(), RcmCalibrationVariable.class);
  }
}
