package com.ak.comm.core;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.interceptor.BytesInterceptor;

import javax.annotation.Nonnegative;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public abstract class AbstractConvertableService<T, R, V extends Enum<V> & Variable<V>>
    extends AbstractService<int[]> implements Callable<Optional<AsynchronousFileChannel>>, Readable {
  private final BytesInterceptor<T, R> bytesInterceptor;
  private final Converter<R, V> responseConverter;
  private final ByteBuffer workingBuffer;
  private final ConcurrentAsyncFileChannel convertedLogByteChannel = new ConcurrentAsyncFileChannel(this);

  protected AbstractConvertableService(BytesInterceptor<T, R> bytesInterceptor,
                                       Converter<R, V> responseConverter) {
    this.bytesInterceptor = Objects.requireNonNull(bytesInterceptor);
    this.responseConverter = Objects.requireNonNull(responseConverter);
    workingBuffer = ByteBuffer.allocate(responseConverter.variables().size() * Integer.BYTES);
  }

  @OverridingMethodsMustInvokeSuper
  @Override
  public void close() {
    responseConverter.close();
    convertedLogByteChannel.close();
  }

  @Override
  public final void read(ByteBuffer dst, @Nonnegative long position) {
    convertedLogByteChannel.read(dst, position);
  }

  protected final void process(ByteBuffer buffer, Consumer<int[]> doAfter) {
    if (buffer.limit() == 0) {
      convertedLogByteChannel.close();
      responseConverter.refresh(false);
    }
    else {
      bytesInterceptor.apply(buffer).flatMap(responseConverter).forEach(ints -> {
        workingBuffer.clear();
        for (int i : ints) {
          workingBuffer.putInt(i);
        }
        workingBuffer.flip();
        convertedLogByteChannel.write(workingBuffer);
        doAfter.accept(ints);
      });
    }
  }

  protected final BytesInterceptor<T, R> bytesInterceptor() {
    return bytesInterceptor;
  }
}