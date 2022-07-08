package com.ak.fx.desktop;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile({"rcm", "rcm-calibration", "nmis",
    "aper2-nibp", "aper2-ecg",
    "aper1-nibp", "aper1-myo", "aper1-R2-6mm", "aper1-R2-7mm", "aper1-R2-8mm", "aper1-R2-10mm", "aper1-R1", "aper1-calibration",
    "kleiber-myo", "prv", "prv_rr"})
public final class DefaultViewController<T, R, V extends Enum<V> & Variable<V>> extends AbstractViewController<T, R, V> {
  @Inject
  @ParametersAreNonnullByDefault
  public DefaultViewController(Provider<BytesInterceptor<T, R>> interceptorProvider, Provider<Converter<R, V>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }
}
