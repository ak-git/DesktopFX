package com.ak.comm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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
import com.ak.comm.file.AutoFileReadingService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.serial.CycleSerialService;
import com.ak.comm.serial.Refreshable;
import org.reactivestreams.Subscriber;

public final class GroupService<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> extends AbstractService
    implements Refreshable {
  @Nonnull
  private final CycleSerialService<RESPONSE, REQUEST, EV> serialService;
  @Nonnull
  private final AutoFileReadingService<RESPONSE, REQUEST, EV> fileReadingService;
  @Nonnull
  private final List<EV> variables;
  @Nonnull
  private final double frequency;

  @Inject
  public GroupService(@Nonnull Provider<BytesInterceptor<RESPONSE, REQUEST>> interceptorProvider,
                      @Nonnull Provider<Converter<RESPONSE, EV>> converterProvider) {
    Converter<RESPONSE, EV> converter = converterProvider.get();
    variables = converter.variables();
    frequency = converter.getFrequency();
    serialService = new CycleSerialService<>(interceptorProvider.get(), converter);
    fileReadingService = new AutoFileReadingService<>(interceptorProvider, converterProvider);
  }

  public boolean isAccept(@Nonnull File file, @Nonnull Subscriber<int[]> subscriber) {
    return fileReadingService.isAccept(file, subscriber);
  }

  @Override
  public void refresh() {
    serialService.refresh();
  }

  public void subscribeSerial(@Nonnull Subscriber<int[]> subscriber) {
    serialService.subscribe(subscriber);
  }

  public List<EV> getVariables() {
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
    fileReadingService.read(buffer, frameSize * from);
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