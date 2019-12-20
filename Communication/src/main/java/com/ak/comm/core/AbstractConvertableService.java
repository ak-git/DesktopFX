package com.ak.comm.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;

public abstract class AbstractConvertableService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractService implements Callable<AsynchronousFileChannel>, Flow.Publisher<int[]>, Readable {
  @Nonnull
  private final BytesInterceptor<T, R> bytesInterceptor;
  @Nonnull
  private final Converter<R, V> responseConverter;
  @Nonnull
  private final ByteBuffer workingBuffer;
  @Nonnull
  private final ConcurrentAsyncFileChannel convertedLogByteChannel = new ConcurrentAsyncFileChannel(this);

  public AbstractConvertableService(@Nonnull BytesInterceptor<T, R> bytesInterceptor,
                                    @Nonnull Converter<R, V> responseConverter) {
    this.bytesInterceptor = bytesInterceptor;
    this.responseConverter = responseConverter;
    workingBuffer = ByteBuffer.allocate(responseConverter.variables().size() * Integer.BYTES);
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void close() {
    convertedLogByteChannel.close();
  }

  @Override
  public final void read(@Nonnull ByteBuffer dst, @Nonnegative long position) {
    convertedLogByteChannel.read(dst, position);
  }

  protected final Stream<int[]> process(@Nonnull ByteBuffer buffer) {
    if (buffer.limit() == 0) {
      convertedLogByteChannel.close();
      responseConverter.refresh();
      return Stream.empty();
    }
    else {
      return bytesInterceptor.apply(buffer).flatMap(responseConverter).peek(ints -> {
        workingBuffer.clear();
        for (int i : ints) {
          workingBuffer.putInt(i);
        }
        workingBuffer.flip();
        convertedLogByteChannel.write(workingBuffer);
      });
    }
  }

  protected final BytesInterceptor<T, R> bytesInterceptor() {
    return bytesInterceptor;
  }
}