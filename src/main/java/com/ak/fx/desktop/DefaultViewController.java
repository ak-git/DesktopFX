package com.ak.fx.desktop;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"rcm", "rcm-calibration", "nmis",
    "aper2-nibp", "aper2-ecg",
    "aper1-nibp", "aper1-myo", "aper1-R2-6mm", "aper1-R2-7mm", "aper1-R2-8mm", "aper1-R2-10mm", "aper1-R1", "aper1-calibration",
    "kleiber-myo", "prv", "NMI3Acc2Rheo"})
public final class DefaultViewController<T, R, V extends Enum<V> & Variable<V>> extends AbstractViewController<T, R, V> {
  @Inject
  public DefaultViewController(Provider<BytesInterceptor<T, R>> interceptorProvider, Provider<Converter<R, V>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }
}
