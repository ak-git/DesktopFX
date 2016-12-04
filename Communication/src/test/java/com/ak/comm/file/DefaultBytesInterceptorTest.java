package com.ak.comm.file;

import java.nio.file.Path;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.DefaultBytesInterceptor;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.Assert;
import org.testng.annotations.Test;

import static jssc.SerialPort.BAUDRATE_115200;

public final class DefaultBytesInterceptorTest {
  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "files")
  public void testDefaultBytesInterceptor(@Nonnull Path fileToRead, @Nonnegative int bytes) throws Exception {
    TestSubscriber<Integer> testSubscriber = TestSubscriber.create();
    BytesInterceptor<Integer, Byte> interceptor = new DefaultBytesInterceptor();
    interceptor.putOut((byte) 1);
    Assert.assertEquals(interceptor.getBaudRate(), BAUDRATE_115200);
    Assert.assertEquals(interceptor.getPingRequest(), Byte.valueOf((byte) 0));

    Flowable.fromPublisher(new FileReadingService(fileToRead)).flatMapIterable(buffer -> () -> interceptor.apply(buffer).iterator()).
        subscribe(testSubscriber);
    testSubscriber.assertNoErrors();

    if (bytes < 0) {
      testSubscriber.assertNoValues();
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertValueCount(bytes);
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
  }
}