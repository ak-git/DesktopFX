package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractConvertableService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractService implements Callable<SeekableByteChannel> {
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private final Converter<RESPONSE, EV> responseConverter;
  @Nonnull
  private final SafeByteChannel convertedLogByteChannel = new SafeByteChannel(this);
  @Nonnull
  private final ByteBuffer workingBuffer;

  public AbstractConvertableService(@Nonnull BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor,
                                    @Nonnull Converter<RESPONSE, EV> responseConverter) {
    this.bytesInterceptor = bytesInterceptor;
    this.responseConverter = responseConverter;
    workingBuffer = ByteBuffer.allocate(responseConverter.variables().size() * Integer.BYTES);
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void close() {
    convertedLogByteChannel.close();
  }

  protected final Stream<int[]> process(@Nonnull ByteBuffer buffer) {
    return bytesInterceptor.apply(buffer).flatMap(responseConverter::apply).peek(ints -> {
      workingBuffer.clear();
      for (int i : ints) {
        workingBuffer.putInt(i);
      }
      workingBuffer.flip();
      convertedLogByteChannel.write(workingBuffer);
    });
  }

  protected final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }
}