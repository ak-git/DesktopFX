package com.ak.comm.file;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.util.Strings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AutoFileReadingServiceTest {
  private static final AutoFileReadingService<BufferFrame, BufferFrame, TwoVariables> SERVICE = new AutoFileReadingService<>(
      () ->
          new RampBytesInterceptor(RampBytesInterceptor.class.getSimpleName(),
              BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES
          ),
      () -> new ToIntegerConverter<>(TwoVariables.class, 1000)
  );

  @BeforeAll
  static void setUp() {
    SERVICE.subscribe(new Flow.Subscriber<>() {
      @Override
      public void onSubscribe(Flow.Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(int[] item) {
        assertThat(item).isNotEmpty();
      }

      @Override
      public void onError(Throwable t) {
        fail(t.getMessage(), t);
      }

      @Override
      public void onComplete() {
        fail();
      }
    });
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#parallelRampFiles")
  void testAccept(Path file) {
    assertTrue(SERVICE.accept(file.toFile()));
    int countFrames = 10;
    ByteBuffer buffer = ByteBuffer.allocate(TwoVariables.values().length * Integer.BYTES * countFrames);
    while (!Thread.currentThread().isInterrupted()) {
      buffer.clear();
      SERVICE.read(buffer, 0);
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
    assertFalse(SERVICE.accept(Paths.get(Strings.EMPTY).toFile()));
  }

  @Override
  public String toString() {
    return "AutoFileReadingServiceTest-${version}}";
  }
}