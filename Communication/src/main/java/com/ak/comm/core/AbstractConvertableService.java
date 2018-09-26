package com.ak.comm.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.OutputBuilders;
import com.ak.comm.util.LogUtils;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;

public abstract class AbstractConvertableService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    extends AbstractService implements Callable<AsynchronousFileChannel>, Flow.Publisher<int[]>, Readable {
  @Nonnull
  private final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor;
  @Nonnull
  private final Converter<RESPONSE, EV> responseConverter;
  @Nonnull
  private final ByteBuffer workingBuffer;
  @Nonnull
  private final ConcurrentAsyncFileChannel convertedLogByteChannel = new ConcurrentAsyncFileChannel(this);
  @Nonnull
  private final Lock txtCollectorLock = new ReentrantLock();
  @Nullable
  private LineFileCollector txtCollector;

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
    closeOutFileCollector();
  }

  @Override
  public final void read(@Nonnull ByteBuffer dst, @Nonnegative long position) {
    convertedLogByteChannel.read(dst, position);
  }

  protected final Stream<int[]> process(@Nonnull ByteBuffer buffer) {
    if (buffer.limit() == 0) {
      convertedLogByteChannel.close();
      responseConverter.refresh();
      closeOutFileCollector();
      return Stream.empty();
    }
    else {
      return bytesInterceptor.apply(buffer).flatMap(responseConverter::apply).peek(ints -> {
        workingBuffer.clear();
        for (int i : ints) {
          workingBuffer.putInt(i);
        }
        workingBuffer.flip();
        convertedLogByteChannel.write(workingBuffer);
//        writeOutFileCollector(ints);
      });
    }
  }

  protected final BytesInterceptor<RESPONSE, REQUEST> bytesInterceptor() {
    return bytesInterceptor;
  }

  private void writeOutFileCollector(int[] ints) {
    txtCollectorLock.lock();
    try {
      try {
        if (txtCollector == null) {
          Path path = OutputBuilders.build(Strings.EMPTY).getPath();
          txtCollector = new LineFileCollector(path, LineFileCollector.Direction.VERTICAL);
          txtCollector.accept(responseConverter.variables().stream().map(Variables::toName).collect(Collectors.joining(Strings.TAB)));
          Logger.getLogger(getClass().getName()).log(Level.INFO, path.toString());
        }
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      }
      if (txtCollector != null) {
        txtCollector.accept(Arrays.stream(ints).mapToObj(Integer::toString).collect(Collectors.joining(Strings.TAB)));
      }
    }
    finally {
      txtCollectorLock.unlock();
    }
  }

  private void closeOutFileCollector() {
    txtCollectorLock.lock();
    try {
      try {
        if (txtCollector != null) {
          txtCollector.close();
        }
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(LogUtils.LOG_LEVEL_ERRORS, e.getMessage(), e);
      }
      finally {
        txtCollector = null;
      }
    }
    finally {
      txtCollectorLock.unlock();
    }
  }
}