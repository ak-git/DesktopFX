package com.ak.comm.file;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.converter.ToIntegerConverter;
import com.ak.comm.converter.TwoVariables;
import com.ak.comm.converter.Variable;
import com.ak.comm.core.AbstractConvertableService;
import com.ak.comm.interceptor.AbstractBytesInterceptor;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.interceptor.simple.RampBytesInterceptor;
import com.ak.comm.logging.LogTestUtils;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.logging.LogBuilders;
import com.ak.util.Clean;
import com.ak.util.Strings;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FileReadingServiceTest {
  private static final Logger LOGGER = Logger.getLogger(FileReadingService.class.getName());

  @BeforeAll
  @AfterAll
  static void setUp() throws IOException {
    Path path = LogBuilders.CONVERTER_FILE.build(Strings.EMPTY).getPath().getParent();
    assertNotNull(path);
    Clean.clean(path);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#rampFiles")
  void testNoDataConverted(Path fileToRead, int bytes) {
    try (FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher = new FileReadingService<>(fileToRead,
        new AbstractBytesInterceptor<>(getClass().getName(),
            BytesInterceptor.BaudRate.BR_921600, 1) {
          @Override
          protected Collection<BufferFrame> innerProcessIn(ByteBuffer src) {
            return Collections.emptyList();
          }
        },
        new ToIntegerConverter<>(TwoVariables.class, 1000)
    )) {
      TestSubscriber<int[]> testSubscriber = new TestSubscriber<>();
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
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#rampFile")
  void testFile(Path fileToRead, @Nonnegative int bytes, boolean forceClose) {
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>();
    int frameLength = 1 + TwoVariables.values().length * Integer.BYTES;
    try (FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher = new FileReadingService<>(
        fileToRead,
        new RampBytesInterceptor(getClass().getName(), BytesInterceptor.BaudRate.BR_921600, frameLength),
        new ToIntegerConverter<>(TwoVariables.class, 200))) {
      LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
          publisher.subscribe(testSubscriber), ignoreLogRecord -> {
        if (forceClose) {
          publisher.close();
        }
      });
    }
    if (forceClose) {
      testSubscriber.assertValueCount(getBlockSize(fileToRead) / frameLength);
    }
    else {
      testSubscriber.assertValueCount(bytes / frameLength);
    }
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#rampFiles")
  void testFiles(Path fileToRead, int bytes) {
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>();
    int frameLength = 1 + TwoVariables.values().length * Integer.BYTES;
    try (FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher =
             new FileReadingService<>(fileToRead, new RampBytesInterceptor(getClass().getName(),
                 BytesInterceptor.BaudRate.BR_921600, frameLength),
                 new ToIntegerConverter<>(TwoVariables.class, 1000))) {
      assertThat(publisher.toString()).contains(fileToRead.toString());

      assertThat(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_BYTES, () ->
          publisher.subscribe(testSubscriber), new Consumer<>() {
        int packCounter;

        @Override
        public void accept(LogRecord logRecord) {
          int blockSize = getBlockSize(fileToRead);
          int bytesCount = (bytes - packCounter * blockSize) >= blockSize ? blockSize : bytes % blockSize;
          assertThat(logRecord.getMessage()).endsWith(bytesCount + " bytes IN from hardware");
          packCounter++;
        }
      })).isEqualTo(bytes > 0);
    }
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

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#filesCanDelete")
  void testException(Path fileToRead, int bytes) {
    assertThat(LogTestUtils.isSubstituteLogLevel(LOGGER, Level.WARNING, () -> {
      TestSubscriber<int[]> testSubscriber = new TestSubscriber<>(ignoreSubscription -> {
        try {
          Files.deleteIfExists(fileToRead);
        }
        catch (IOException e) {
          fail(fileToRead.toString(), e);
        }
      });

      try (FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher =
               new FileReadingService<>(fileToRead, new RampBytesInterceptor(getClass().getName(),
                   BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
                   new ToIntegerConverter<>(TwoVariables.class, 200))
      ) {
        publisher.subscribe(testSubscriber);
      }
      if (bytes < 0) {
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotSubscribed();
      }
      else {
        assertThat(testSubscriber.throwable).isNotNull().extracting(Throwable::getClass).isEqualTo(NoSuchFileException.class);
        testSubscriber.assertSubscribed();
      }
      testSubscriber.assertNoValues();
      testSubscriber.assertNotComplete();
    }, logRecord -> assertThat(fileToRead).hasToString(logRecord.getMessage()))).isEqualTo(bytes > -1);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.file.FileDataProvider#rampFiles")
  void testCancel(Path fileToRead, int bytes) {
    TestSubscriber<int[]> testSubscriber = new TestSubscriber<>(Flow.Subscription::cancel);
    try (FileReadingService<BufferFrame, BufferFrame, TwoVariables> publisher =
             new FileReadingService<>(fileToRead, new RampBytesInterceptor(getClass().getName(),
                 BytesInterceptor.BaudRate.BR_921600, 1 + TwoVariables.values().length * Integer.BYTES),
                 new ToIntegerConverter<>(TwoVariables.class, 1000))
    ) {
      publisher.subscribe(testSubscriber);
    }
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
  void testInvalidChannelCall() throws Exception {
    try (var s = new FileReadingService<>(Paths.get(Strings.EMPTY), new RampBytesInterceptor(getClass().getName(),
        BytesInterceptor.BaudRate.BR_115200, 1 + TwoVariables.values().length * Integer.BYTES),
        new ToIntegerConverter<>(TwoVariables.class, 200))
    ) {
      assertTrue(s.call().isEmpty());
    }
  }

  @Test
  void testAbstractConvertableService() {
    try (AbstractConvertableService<BufferFrame, BufferFrame, TestVariable> convertableService =
             new AbstractConvertableService<>(new RampBytesInterceptor(getClass().getName(),
                 BytesInterceptor.BaudRate.BR_460800, 1 + TestVariable.values().length * Integer.BYTES),
                 new ToIntegerConverter<>(TestVariable.class, 2)) {
               @Override
               public void subscribe(Flow.Subscriber<? super int[]> subscriber) {
                 subscriber.onSubscribe(new Flow.Subscription() {
                   @Override
                   public void request(long n) {
                     assertThat(n).isEqualTo(Long.MAX_VALUE);
                   }

                   @Override
                   public void cancel() {
                     fail();
                   }
                 });
                 process(ByteBuffer.allocate(0), ints -> fail(Arrays.toString(ints)));
                 process(ByteBuffer.wrap(new byte[] {0, 2, 0, 0, 0, 1}), ints -> assertThat(ints).containsExactly(new int[] {2}));
                 process(ByteBuffer.wrap(new byte[] {4, 0, 0, 0, 2}), ints -> assertThat(ints).containsExactly(new int[] {3}));
                 process(ByteBuffer.allocate(0), ints -> fail(Arrays.toString(ints)));
                 process(ByteBuffer.wrap(new byte[] {6, 0, 0, 0, 3}), ints -> assertThat(ints).containsExactly(new int[] {6}));
                 subscriber.onComplete();
               }

               @Override
               public Optional<AsynchronousFileChannel> call() throws Exception {
                 return Optional.of(
                     AsynchronousFileChannel.open(LogBuilders.CONVERTER_FILE.build(TestVariable.V_RRS.name()).getPath(),
                         StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING)
                 );
               }

               @Override
               public void refresh(boolean force) {
                 throw new UnsupportedOperationException();
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
      fail();
    }
  }

  @Nonnegative
  private static int getBlockSize(Path fileToRead) {
    int blockSize = 0;
    try {
      blockSize = (int) Files.getFileStore(fileToRead).getBlockSize();
    }
    catch (IOException e) {
      fail(fileToRead.toString(), e);
    }
    return blockSize;
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
    private final Consumer<Flow.Subscription> onSubscribe;
    private boolean subscribeFlag;
    private boolean completeFlag;
    private @Nullable Throwable throwable;
    private int count;

    TestSubscriber() {
      this(subscription -> subscription.request(Long.MAX_VALUE));
    }

    TestSubscriber(Consumer<Flow.Subscription> onSubscribe) {
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

    @Override
    public String toString() {
      return "TestSubscriber-${version}";
    }

    void assertSubscribed() {
      assertTrue(subscribeFlag);
    }

    void assertNotSubscribed() {
      assertFalse(subscribeFlag);
    }

    void assertComplete() {
      assertTrue(completeFlag);
    }

    void assertNotComplete() {
      assertFalse(completeFlag);
    }

    void assertNoValues() {
      assertValueCount(0);
    }

    void assertValueCount(int n) {
      assertThat(count).isEqualTo(n);
    }

    void assertNoErrors() {
      if (throwable != null) {
        fail(throwable.getMessage(), throwable);
      }
    }
  }
}