package com.ak.comm;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class GroupServiceTest implements Flow.Subscriber<int[]> {
  private final GroupService<BufferFrame, BufferFrame, TwoVariables> service = new GroupService<>(
      () -> new RampBytesInterceptor(RampBytesInterceptor.class.getSimpleName(),
          BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
      () -> new ToIntegerConverter<>(TwoVariables.class, 1000));
  @Nullable
  private Flow.Subscription subscription;

  @BeforeEach
  void setUp() {
    service.subscribe(this);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#rampFiles2")
  void testRead(@Nonnull Path file) {
    assertTrue(service.accept(file.toFile()));
    while (!Thread.currentThread().isInterrupted()) {
      int countFrames = 10;
      int shift = 2;
      int[][] ints = service.read(shift, countFrames + shift);
      if (ints.length != 0) {
        for (int i = 0; i < ints[TwoVariables.V1.ordinal()].length; i++) {
          for (TwoVariables v : TwoVariables.values()) {
            assertThat(ints[v.ordinal()][i]).as(Arrays.toString(ints[v.ordinal()])).isEqualTo(i + v.ordinal() + shift);
          }
        }
        break;
      }
    }
    assertTrue(Arrays.stream(service.read(0, 0)).allMatch(ints -> ints.length == 0));
    service.refresh(false);
  }

  @AfterEach
  void tearDown() {
    service.close();
  }

  @Override
  public void onSubscribe(Flow.Subscription s) {
    if (subscription != null) {
      subscription.cancel();
    }
    subscription = s;
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
}