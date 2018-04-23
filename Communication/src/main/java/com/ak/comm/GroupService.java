package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.Readable;
import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.comm.serial.Refreshable;

public final class GroupService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> extends AbstractService
    implements Flow.Publisher<int[]>, Refreshable, FileFilter {
  @Nonnull
  private final CycleSerialService<RESPONSE, REQUEST, EV> serialService;
  @Nonnull
  private final AutoFileReadingService<RESPONSE, REQUEST, EV> fileReadingService;
  @Nonnull
  private final List<EV> variables;
  @Nonnull
  private final double frequency;
  @Nonnull
  private volatile Readable currentReadable;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                      @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    Converter<RESPONSE, EV> converter = converterProvider.get();
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
  public void refresh() {
    serialService.refresh();
    currentReadable = serialService;
  }

  public int write(@Nullable REQUEST request) {
    if (Objects.equals(currentReadable, serialService)) {
      return serialService.write(request);
    }
    else {
      return -1;
    }
  }

  public List<EV> getVariables() {
    return Collections.unmodifiableList(variables);
  }

  public double getFrequency() {
    return frequency;
  }

  @Override
  public void close() {
    serialService.close();
    fileReadingService.close();
  }

  public Map<EV, int[]> read(@Nonnegative int fromInclusive, @Nonnegative int toExclusive) {
    int from = Math.max(0, Math.min(fromInclusive, toExclusive));
    int to = Math.max(0, Math.max(fromInclusive, toExclusive));

    int frameSize = variables.size() * Integer.BYTES;
    ByteBuffer buffer = ByteBuffer.allocate(frameSize * (to - from));
    currentReadable.read(buffer, frameSize * from);
    buffer.flip();

    int count = buffer.limit() / frameSize;
    Map<EV, int[]> result = variables.stream().collect(Collectors.toMap(o -> o, o -> new int[count]));
    for (int i = 0; i < count; i++) {
      for (EV variable : variables) {
        result.get(variable)[i] = buffer.getInt();
      }
    }
    return result;
  }
}