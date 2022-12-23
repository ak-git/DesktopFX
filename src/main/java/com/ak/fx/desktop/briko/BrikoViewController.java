package com.ak.fx.desktop.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.briko.BrikoVariable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@Component
@Profile("briko")
public final class BrikoViewController extends AbstractViewController<BufferFrame, BufferFrame, BrikoVariable> {
  @Inject
  @ParametersAreNonnullByDefault
  public BrikoViewController(@Named("briko-interceptor") Provider<BytesInterceptor<BufferFrame, BufferFrame>> interceptorProvider,
                             @Named("briko-converter") Provider<Converter<BufferFrame, BrikoVariable>> converterProvider) {
    super(interceptorProvider, converterProvider);
  }
}
