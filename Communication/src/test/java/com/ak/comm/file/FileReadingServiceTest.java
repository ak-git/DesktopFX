package com.ak.comm.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.logging.OutputBuilders;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.logging.LogBuilders;
import com.ak.util.LogUtils;
import com.ak.util.PropertiesSupport;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class FileReadingServiceTest {
  private static final Logger LOGGER = Logger.getLogger(FileReadingService.class.getName());
  private static final int CAPACITY_4K = 4096;

  private FileReadingServiceTest() {
  }

  @BeforeSuite
  @AfterSuite
  public static void setUp() throws IOException {
    Path[] paths = {
        LogBuilders.CONVERTER_FILE.build(Strings.EMPTY).getPath().getParent(),
        OutputBuilders.build(Strings.EMPTY).getPath().getParent()
    };
    for (Path path : paths) {
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
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public static void testNoDataConverted(@Nonnull Path fileToRead, int bytes) {
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>();
    Flow.Publisher<int[]> publisher = new FileReadingService<>(fileToRead,
        new AbstractBytesInterceptor<>(
            BytesInterceptor.BaudRate.BR_921600, null, 1) {
          @Nonnull
          @Override
          protected Collection<BufferFrame> innerProcessIn(@Nonnull ByteBuffer src) {
            return Collections.emptyList();
          }
        },
        new ToIntegerConverter<>(TwoVariables.class, 1000)
    );
    publisher.subscribe(testSubscriber);
    testSubscriber.assertNoErrors();
    testSubscriber.assertNoValues();
    if (bytes < 0) {
      testSubscriber.assertNotComplete();
      testSubscriber.assertNotSubscribed();
    }
    else {
      testSubscriber.assertComplete();
      testSubscriber.assertSubscribed();
    }
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFile")
  public static void testFile(@Nonnull Path fileToRead, @Nonnegative int bytes, boolean forceClose) {
    PropertiesSupport.CACHE.set(Boolean.valueOf(!forceClose).toString());
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>();
    int frameLength = 1 + TwoVariables.values().length * Integer.BYTES;
    FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher = new FileReadingService<>(
        fileToRead,
        new RampBytesInterceptor(BytesInterceptor.BaudRate.BR_921600, frameLength),
        new ToIntegerConverter<>(TwoVariables.class, 200));
    LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
        publisher.subscribe(testSubscriber), logRecord -> {
      if (forceClose) {
        publisher.close();
      }
    });
    testSubscriber.assertValueCount(bytes / frameLength);
    PropertiesSupport.CACHE.clear();
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public static void testFiles(@Nonnull Path fileToRead, int bytes) {
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>();
    int frameLength = 1 + TwoVariables.values().length * Integer.BYTES;
    Flow.Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_921600, frameLength),
        new ToIntegerConverter<>(TwoVariables.class, 1000));
    Assert.assertTrue(publisher.toString().contains(fileToRead.toString()));

    Assert.assertEquals(LogUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
        publisher.subscribe(testSubscriber), new Consumer<>() {
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
  public static void testException(@Nonnull Path fileToRead, int bytes) {
    Assert.assertEquals(LogUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      TestSubscriber<int[]> testSubscriber = new TestSubscriber<>(subscription -> {
        try {
          Files.deleteIfExists(fileToRead);
        }
        catch (IOException e) {
          Assert.fail(fileToRead.toString(), e);
        }
      });

      Flow.Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
          BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
          new ToIntegerConverter<>(TwoVariables.class, 200));
      publisher.subscribe(testSubscriber);
      if (bytes < 0) {
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotSubscribed();
      }
      else {
        Assert.assertEquals(Objects.requireNonNull(testSubscriber.throwable).getClass(), NoSuchFileException.class);
        testSubscriber.assertSubscribed();
      }
      testSubscriber.assertNoValues();
      testSubscriber.assertNotComplete();
    }, logRecord -> Assert.assertEquals(logRecord.getMessage(), fileToRead.toString())), bytes > -1);
  }

  @Test(dataProviderClass = FileDataProvider.class, dataProvider = "rampFiles")
  public static void testCancel(@Nonnull Path fileToRead, int bytes) {
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>(Flow.Subscription::cancel);
    Flow.Publisher<int[]> publisher = new FileReadingService<>(fileToRead, new RampBytesInterceptor(
        BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class, 1000));
    publisher.subscribe(testSubscriber);
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
        new ToIntegerConverter<>(TwoVariables.class, 200)).call());
  }

  @Test
  public static void testAbstractConvertableService() {
    try (AbstractConvertableService<BufferFrame, BufferFrame, TestVariable> convertableService =
             new AbstractConvertableService<>(new RampBytesInterceptor(
                 BytesInterceptor.BaudRate.BR_460800, 1 + TestVariable.values().length * Integer.BYTES),
                 new ToIntegerConverter<>(TestVariable.class, 2)) {
               @Override
               public void subscribe(Flow.Subscriber<? super int[]> subscriber) {
                 subscriber.onSubscribe(new Flow.Subscription() {
                   @Override
                   public void request(long n) {
                     Assert.assertEquals(n, Long.MAX_VALUE);
                   }

                   @Override
                   public void cancel() {
                     Assert.fail();
                   }
                 });
                 Assert.assertEquals(process(ByteBuffer.allocate(0)).count(), 0);
                 int[] ints = process(ByteBuffer.wrap(new byte[] {0, 2, 0, 0, 0, 1})).mapToInt(value -> value[0]).toArray();
                 Assert.assertEquals(ints, new int[] {2});
                 ints = process(ByteBuffer.wrap(new byte[] {4, 0, 0, 0, 2})).mapToInt(value -> value[0]).toArray();
                 Assert.assertEquals(ints, new int[] {3});

                 Assert.assertEquals(process(ByteBuffer.allocate(0)).count(), 0);
                 ints = process(ByteBuffer.wrap(new byte[] {6, 0, 0, 0, 3})).mapToInt(value -> value[0]).toArray();
                 Assert.assertEquals(ints, new int[] {6});
                 subscriber.onComplete();
               }

               @Override
               public AsynchronousFileChannel call() throws Exception {
                 return AsynchronousFileChannel.open(LogBuilders.CONVERTER_FILE.build(TestVariable.V_RRS.name()).getPath(),
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING);
               }
             }) {
      TestSubscriber<int[]> subscriber = new TestSubscriber<>();
      convertableService.subscribe(subscriber);
      subscriber.assertNoErrors();
      subscriber.assertSubscribed();
      subscriber.assertComplete();
      subscriber.assertValueCount(0);
    }
    catch (Exception ex) {
      Assert.fail();
    }
  }

  private enum TestVariable implements Variable<TestVariable> {
    V_RRS {
      @Override
      public DigitalFilter filter() {
        return FilterBuilder.of().rrs().build();
      }
    }
  }

  private static final class TestSubscriber<T> implements Flow.Subscriber<T> {
    @Nonnull
    private final Consumer<Flow.Subscription> onSubscribe;
    private boolean subscribeFlag;
    private boolean completeFlag;
    @Nullable
    private Throwable throwable;
    private int count;


    TestSubscriber() {
      this(subscription -> subscription.request(Long.MAX_VALUE));
    }

    TestSubscriber(@Nonnull Consumer<Flow.Subscription> onSubscribe) {
      this.onSubscribe = onSubscribe;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      subscribeFlag = true;
      onSubscribe.accept(subscription);
    }

    @Override
    public void onNext(T item) {
      count++;
    }

    @Override
    public void onError(Throwable throwable) {
      this.throwable = throwable;
    }

    @Override
    public void onComplete() {
      completeFlag = true;
    }

    void assertSubscribed() {
      Assert.assertTrue(subscribeFlag);
    }

    void assertNotSubscribed() {
      Assert.assertFalse(subscribeFlag);
    }

    void assertComplete() {
      Assert.assertTrue(completeFlag);
    }

    void assertNotComplete() {
      Assert.assertFalse(completeFlag);
    }

    void assertNoValues() {
      assertValueCount(0);
    }

    void assertValueCount(int n) {
      Assert.assertEquals(count, n);
    }

    void assertNoErrors() {
      if (throwable != null) {
        Assert.fail(throwable.getMessage(), throwable);
      }
    }
  }
}