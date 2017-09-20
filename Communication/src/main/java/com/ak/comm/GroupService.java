package com.ak.comm;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public final class GroupService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> extends AbstractService
    implements Publisher<int[]>, Refreshable, FileFilter {
  @Nonnull
  private final CycleSerialService<RESPONSE, REQUEST, EV> serialService;
  @Nonnull
  private final AutoFileReadingService<RESPONSE, REQUEST, EV> fileReadingService;
  @Nonnull
  private final Collection<EV> variables;
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
  public void subscribe(@Nonnull Subscriber<? super int[]> subscriber) {
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

  public Collection<EV> getVariables() {
    return variables;
  }

  public double getFrequency() {
    return frequency;
  }

  @Override
  public void close() throws IOException {
    serialService.close();
    fileReadingService.close();
  }

  public List<int[]> read(@Nonnegative int fromInclusive, @Nonnegative int toExclusive) {
    int from = Math.min(fromInclusive, toExclusive);
    int to = Math.max(fromInclusive, toExclusive);

    int frameSize = variables.size() * Integer.BYTES;
    ByteBuffer buffer = ByteBuffer.allocate(frameSize * (to - from));
    currentReadable.read(buffer, frameSize * from);
    buffer.flip();

    int count = buffer.limit() / frameSize;
    if (count == 0) {
      return Collections.emptyList();
    }
    else {
      List<int[]> result = variables.stream().map(ev -> new int[count]).collect(Collectors.toList());
      for (int i = 0; i < count; i++) {
        for (int[] ints : result) {
          ints[i] = buffer.getInt();
        }
      }
      return result;
    }
  }
}