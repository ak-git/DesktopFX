package com.ak.comm.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.logging.LogBuilders;
import com.ak.comm.util.LogUtils;
import com.ak.digitalfilter.Frequencies;
import com.ak.util.Strings;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileReadingServiceTest {
  private static final Logger LOGGER = Logger.getLogger(FileReadingService.class.getName());
  private static final int CAPACITY_4K = 4096;

  private FileReadingServiceTest() {
  }

  @BeforeClass
  @AfterClass
  public static void setUp() throws IOException {
    Path path = LogBuilders.CONVERTER_FILE.build(Strings.EMPTY).getPath().getParent();
    Assert.assertNotNull(path);
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, entry -> Files.isRegularFile(entry))) {
      for (Path file : ds) {
        try {
          Files.deleteIfExists(file);
        }
        catch (IOException e) {
          Assert.fail(file.toString(), e);
        }
      }
    }
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFile")
  public static void testFile(@Nonnull Path fileToRead, @Nonnegative int bytes, boolean forceClose) {
    TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
    int frameLength = 1 + TwoVariables.values().length * Integer.BYTES;
    FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher = new FileReadingService<>(
        fileToRead,
        new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_921600, frameLength),
        new ToIntegerConverter<>(TwoVariables.class, Frequencies.HZ_200));
    LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
        Flowable.fromPublisher(publisher).subscribe(testSubscriber), logRecord -> {
      if (forceClose) {
        try {
          publisher.close();
        }
        catch (IOException e) {
          Assert.fail(e.getMessage(), e);
        }
      }
    });
    testSubscriber.assertValueCount(bytes / frameLength);
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public static void testFiles(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
    int frameLength = 1 + TwoVariables.values().length * Integer.BYTES;
    Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_921600, frameLength),
        new ToIntegerConverter<>(TwoVariables.class, Frequencies.HZ_1000));
    Assert.assertTrue(publisher.toString().contains(fileToRead.toString()));

    Assert.assertEquals(LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
        Flowable.fromPublisher(publisher).subscribe(testSubscriber), new Consumer<LogRecord>() {
      int packCounter;

      @Override
      public void accept(LogRecord logRecord) {
        int bytesCount = (bytes - packCounter * CAPACITY_4K) >= CAPACITY_4K ? CAPACITY_4K : bytes % CAPACITY_4K;
        Assert.assertTrue(logRecord.getMessage().endsWith(bytesCount + " bytes IN from hardware"), logRecord.getMessage());
        packCounter++;
      }
    }), bytes > 0);

    testSubscriber.assertNoErrors();
    if (bytes < 0) {
      testSubscriber.assertNoValues();
      testSubscriber.assertNotSubscribed();
      testSubscriber.assertNotComplete();
    }
    else {
      testSubscriber.assertValueCount(bytes > 0 ? bytes / frameLength - 1 : 0);
      testSubscriber.assertSubscribed();
      testSubscriber.assertComplete();
    }
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "filesCanDelete")
  public static void testException(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    Assert.assertEquals(LogUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
      Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
          BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
          new ToIntegerConverter<>(TwoVariables.class, Frequencies.HZ_200));
      Flowable.fromPublisher(publisher).doOnSubscribe(subscription -> Files.deleteIfExists(fileToRead)).subscribe(testSubscriber);
      if (bytes < 0) {
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotSubscribed();
      }
      else {
        testSubscriber.assertError(NoSuchFileException.class);
        testSubscriber.assertSubscribed();
      }
      testSubscriber.assertNoValues();
      testSubscriber.assertNotComplete();
    }, logRecord -> Assert.assertEquals(logRecord.getMessage(), fileToRead.toString())), bytes > -1);
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public static void testCancel(@Nonnull Path fileToRead, @Nonnegative int bytes) {
    TestSubscriber<int[]> testSubscriber = TestSubscriber.create();
    Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class, Frequencies.HZ_1000));
    Flowable.fromPublisher(publisher).doOnSubscribe(Subscription::cancel).subscribe(testSubscriber);
    testSubscriber.assertNoErrors();
    testSubscriber.assertNoValues();
    testSubscriber.assertNotComplete();
    if (bytes < 0) {
      testSubscriber.assertNotSubscribed();
    }
    else {
      testSubscriber.assertSubscribed();
    }
  }

  @Test
  public static void testInvalidChannelCall() throws Exception {
    Assert.assertNull(new FileReadingService<>(Paths.get(Strings.EMPTY), new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class, Frequencies.HZ_200)).call());
  }
}