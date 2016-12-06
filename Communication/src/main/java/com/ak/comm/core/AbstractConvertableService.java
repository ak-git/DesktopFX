package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.converter.Converter;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.SafeByteChannel;

public abstract class AbstractConvertableService<RESPONSE, REQUEST> extends AbstractService<int[]> {
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private final Converter<RESPONSE> responseConverter;
  private final SafeByteChannel byteChannel = new SafeByteChannel(getClass().getSimpleName());
  private final ByteBuffer workingBuffer = ByteBuffer.allocate(1024);

  public AbstractConvertableService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                    @Nonnull Converter<RESPONSE> responseConverter) {
    this.bytesInterceptor = bytesInterceptor;
    this.responseConverter = responseConverter;
  }

  @Override
  public final void request(long n) {
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void cancel() {
    byteChannel.close();
  }

  protected final Stream<int[]> process(@Nonnull ByteBuffer buffer) {
    return bytesInterceptor.apply(buffer).flatMap(responseConverter::apply).peek(ints -> {
      workingBuffer.clear();
      for (int i : ints) {
        workingBuffer.putInt(i);
      }
      workingBuffer.flip();
      byteChannel.write(workingBuffer);
    });
  }

  protected final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }
}