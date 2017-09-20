package com.ak.comm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.file.FileDataProvider;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GroupServiceTest implements Subscriber<int[]> {
  private final GroupService<BufferFrame, BufferFrame, TwoVariables> service = new GroupService<>(
      () -> new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
      () -> new ToIntegerConverter<>(TwoVariables.class, 1000));

  private GroupServiceTest() {
  }

  @BeforeClass
  public void setUp() {
    service.subscribe(this);
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles2", invocationCount = 10, singleThreaded = true)
  public void testRead(@Nonnull Path file) {
    Assert.assertTrue(service.accept(file.toFile()));
    while (!Thread.currentThread().isInterrupted()) {
      int countFrames = 10;
      int shift = 2;
      List<int[]> ints = service.read(shift, countFrames + shift);
      if (!ints.isEmpty()) {
        for (int i = 0; i < ints.get(0).length; i++) {
          for (int j = 0; j < TwoVariables.values().length; j++) {
            Assert.assertEquals(ints.get(j)[i], i + j + shift, Arrays.toString(ints.get(j)));
          }
        }
        break;
      }
    }
    Assert.assertTrue(service.read(1, 1).isEmpty());
  }

  @AfterClass
  public void tearDown() throws IOException {
    service.close();
  }

  @Override
  public void onSubscribe(Subscription s) {
  }

  @Override
  public void onNext(int[] ints) {
  }

  @Override
  public void onError(Throwable t) {
    Assert.fail(t.getMessage(), t);
  }

  @Override
  public void onComplete() {
  }
}