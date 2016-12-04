package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractConvertableService<RESPONSE, REQUEST> extends AbstractService<int[]> {
  private static final Level LOG_LEVEL_VALUES = Level.FINE;
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
    Stream<int[]> stream = bytesInterceptor.apply(buffer).flatMap(responseConverter::apply);
    if (logger.isLoggable(LOG_LEVEL_VALUES)) {
      stream = stream.peek(ints -> logger.log(LOG_LEVEL_VALUES, String.format("#%x %s", hashCode(), Arrays.toString(ints))));
    }
    return stream;
  }

  protected final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }
}