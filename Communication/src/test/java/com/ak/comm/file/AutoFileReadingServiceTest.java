package com.ak.comm.file;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Flow;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AutoFileReadingServiceTest implements Flow.Subscriber<int[]> {
  private final AutoFileReadingService<BufferFrame, BufferFrame, TwoVariables> service = new AutoFileReadingService<>(
      () -> new RampBytesInterceptor(getClass().getName(), BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
      () -> new ToIntegerConverter<>(TwoVariables.class, 1000));

  @BeforeEach
  public void setUp() {
    service.subscribe(this);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#parallelRampFiles")
  void testAccept(@Nonnull Path file) {
    assertTrue(service.accept(file.toFile()));
    int countFrames = 10;
    ByteBuffer buffer = ByteBuffer.allocate(TwoVariables.values().length * Integer.BYTES * countFrames);
    while (!Thread.currentThread().isInterrupted()) {
      buffer.clear();
      service.read(buffer, 0);
      buffer.flip();
      if (buffer.limit() == buffer.capacity()) {
        for (int i = 0; i < countFrames; i++) {
          for (int j = 0; j < TwoVariables.values().length; j++) {
            assertThat(buffer.getInt()).isEqualTo(i + j);
          }
        }
        break;
      }
    }
  }

  @Test
  void testNotAccept() {
    assertFalse(service.accept(Paths.get(Strings.EMPTY).toFile()));
  }

  @Override
  public void onSubscribe(Flow.Subscription s) {
    s.request(Long.MAX_VALUE);
  }

  @Override
  public void onNext(int[] ints) {
  }

  @Override
  public void onError(Throwable t) {
    fail(t.getMessage(), t);
  }

  @Override
  public void onComplete() {
  }

  @Override
  public String toString() {
    return "AutoFileReadingServiceTest-${version}}";
  }
}