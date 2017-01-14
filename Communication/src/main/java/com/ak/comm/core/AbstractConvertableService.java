package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractConvertableService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable> extends AbstractService<int[]> {
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private final Converter<RESPONSE, EV> responseConverter;
  private final SafeByteChannel byteChannel = new SafeByteChannel(getClass().getSimpleName());
  @Nonnull
  private final ByteBuffer workingBuffer;

  public AbstractConvertableService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                    @Nonnull Converter<RESPONSE, EV> responseConverter) {
    this.bytesInterceptor = bytesInterceptor;
    this.responseConverter = responseConverter;
    workingBuffer = ByteBuffer.allocate(responseConverter.variables().size() * Integer.BYTES);
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