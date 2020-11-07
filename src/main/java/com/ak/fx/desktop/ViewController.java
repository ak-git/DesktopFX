package com.ak.fx.desktop;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import org.springframework.context.annotation.Profile;

@Named
@Profile({"rcm", "rcm-calibration", "nmis", "aper2-nibp", "aper1", "aper2", "aper4"})
public final class ViewController<T, R, V extends Enum<V> & Variable<V>> extends AbstractViewController<T, R, V> {
  @Inject
  public ViewController(@Nonnull Provider<BytesInterceptor<T, R>> interceptorProvider,
                        @Nonnull Provider<Converter<R, V>> converterProvider) {
    super(new GroupService<>(interceptorProvider::get, converterProvider::get));
  }
}
