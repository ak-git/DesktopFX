package com.ak.fx.desktop;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@Component
@Profile({"rcm", "rcm-calibration", "nmis",
    "aper2-nibp", "aper1-nibp", "aper1-myo", "aper1-R2-6mm", "aper1-R2-7mm", "aper1-R2-8mm", "aper1-R2-10mm", "aper1-R1", "aper1-calibration",
    "kleiber-myo", "prv", "briko-black", "NMI3Acc2Rheo"})
public final class DefaultViewController<T, R, V extends Enum<V> & Variable<V>> extends AbstractViewController<T, R, V> {
  @Inject
  @ParametersAreNonnullByDefault
  public DefaultViewController(Provider<BytesInterceptor<T, R>> interceptorProvider, Provider<Converter<R, V>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }
}
