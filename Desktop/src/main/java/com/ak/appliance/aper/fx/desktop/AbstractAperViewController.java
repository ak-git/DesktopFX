package com.ak.appliance.aper.fx.desktop;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.fx.desktop.AbstractViewController;
import jakarta.inject.Provider;

abstract class AbstractAperViewController<V extends Enum<V> & Variable<V>> extends AbstractViewController<BufferFrame, BufferFrame, V> {
  AbstractAperViewController(Provider<Converter<BufferFrame, V>> converterProvider) {
    super(
        () -> new RampBytesInterceptor("aper", BytesInterceptor.BaudRate.BR_460800, 25),
        converterProvider
    );
  }
}
