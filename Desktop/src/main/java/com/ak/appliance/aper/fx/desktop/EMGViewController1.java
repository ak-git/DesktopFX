package com.ak.appliance.aper.fx.desktop;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@Component
@Profile("aper2-emg1")
public final class EMGViewController1<T, R, V extends Enum<V> & Variable<V>> extends AbstractViewController<T, R, V> {
  @Inject
  @ParametersAreNonnullByDefault
  public EMGViewController1(Provider<BytesInterceptor<T, R>> interceptorProvider, Provider<Converter<R, V>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }
}
