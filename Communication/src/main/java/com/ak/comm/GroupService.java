package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.logging.LogBuilders;

public final class GroupService<T, R, V extends Enum<V> & Variable<V>> extends AbstractService<int[]> implements FileFilter {
  @Nonnull
  private final CycleSerialService<T, R, V> serialService;
  @Nonnull
  private final AutoFileReadingService<T, R, V> fileReadingService;
  @Nonnull
  private final List<V> variables;
  @Nonnegative
  private final double frequency;
  @Nonnull
  private Readable currentReadable;

  @ParametersAreNonnullByDefault
  public GroupService(Supplier<BytesInterceptor<T, R>> interceptorProvider, Supplier<Converter<R, V>> converterProvider) {
    Converter<R, V> converter = converterProvider.get();
    variables = converter.variables();
    frequency = converter.getFrequency();
    serialService = new CycleSerialService<>(interceptorProvider.get(), converter);
    fileReadingService = new AutoFileReadingService<>(interceptorProvider, converterProvider);
    currentReadable = serialService;
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super int[]> subscriber) {
    serialService.subscribe(subscriber);
    fileReadingService.subscribe(subscriber);
  }

  @Override
  public boolean accept(@Nonnull File file) {
    boolean accept = fileReadingService.accept(file);
    if (accept) {
      currentReadable = fileReadingService;
    }
    return accept;
  }

  @Override
  public void refresh(boolean force) {
    currentReadable.refresh(force);
    if (!Objects.equals(currentReadable, serialService)) {
      serialService.refresh(force);
      currentReadable = serialService;
    }
    LogBuilders.CONVERTER_FILE.clean();
  }

  public void write(@Nullable T request) {
    if (Objects.equals(currentReadable, serialService)) {
      serialService.write(request);
    }
  }

  @Nonnull
  public List<V> getVariables() {
    return Collections.unmodifiableList(variables);
  }

  @Nonnegative
  public double getFrequency() {
    return frequency;
  }

  @Override
  public void close() {
    serialService.close();
    fileReadingService.close();
  }

  @Nonnull
  public int[][] read(@Nonnegative int fromInclusive, @Nonnegative int toExclusive) {
    int from = Math.min(fromInclusive, toExclusive);
    int to = Math.max(fromInclusive, toExclusive);

    int countVariables = variables.size();
    int frameSize = countVariables * Integer.BYTES;
    var buffer = ByteBuffer.allocate(frameSize * (to - from));
    currentReadable.read(buffer, (long) frameSize * from);
    buffer.flip();

    int countData = buffer.limit() / frameSize;
    var result = new int[countVariables][countData];
    for (var i = 0; i < countData; i++) {
      for (var j = 0; j < countVariables; j++) {
        result[j][i] = buffer.getInt();
      }
    }
    return result;
  }
}