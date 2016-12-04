package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractConvertableService<RESPONSE, REQUEST> extends AbstractService<int[]> {
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private final Converter<RESPONSE> responseConverter;

  public AbstractConvertableService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                    @Nonnull Converter<RESPONSE> responseConverter) {
    this.bytesInterceptor = bytesInterceptor;
    this.responseConverter = responseConverter;
  }

  @Override
  public final void request(long n) {
  }

  protected final Stream<int[]> process(ByteBuffer buffer) {
    return bytesInterceptor.apply(buffer).flatMap(responseConverter::apply);
  }

  protected final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }
}