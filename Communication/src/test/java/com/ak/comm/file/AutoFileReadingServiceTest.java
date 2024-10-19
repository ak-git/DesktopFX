package com.ak.comm.file;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.logging.LogTestUtils;
import com.ak.util.Strings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoFileReadingServiceTest {
  private static final Logger LOGGER = Logger.getLogger(AutoFileReadingService.class.getName());
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

  @AfterAll
  static void tearDown() {
    SERVICE.close();
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#parallelRampFiles")
  void testAccept(Path file) {
    assertThat(SERVICE.accept(file.toFile())).isTrue();
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
    assertThat(SERVICE.accept(Paths.get(Strings.EMPTY).toFile())).isFalse();
  }

  @Test
  void testEmptyReadable() {
    SERVICE.refresh(true);
    ByteBuffer allocate = ByteBuffer.allocate(0);
    SERVICE.read(allocate, 1000);
    assertThat(allocate.position()).isZero();
    assertThat(allocate.capacity()).isZero();
  }

  @Nested
  class Mocking {
    @Mock
    private File mockedFile;
    private final AtomicInteger exceptionCounter = new AtomicInteger();

    @ParameterizedTest
    @MethodSource("com.ak.comm.file.FileDataProvider#parallelRampFiles")
    void testAccept(Path file) {
      when(mockedFile.isFile()).thenReturn(true);
      when(mockedFile.getName()).thenReturn(file.toFile().getName());
      when(mockedFile.toPath()).thenThrow(RuntimeException.class);

      assertThat(
          LogTestUtils.isSubstituteLogLevel(LOGGER, Level.WARNING,
              () -> SERVICE.accept(mockedFile),
              logRecord -> {
                assertThat(logRecord.getThrown())
                    .isInstanceOf(CompletionException.class).hasCauseInstanceOf(RuntimeException.class);
                assertThat(logRecord.getMessage()).isEqualTo(file.toFile().getName());
                exceptionCounter.incrementAndGet();
              }
          )
      ).isTrue();
      verify(mockedFile).toPath();
      assertThat(exceptionCounter.get()).isOne();
    }
  }
}