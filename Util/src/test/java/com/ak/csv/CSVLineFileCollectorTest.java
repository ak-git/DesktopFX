package com.ak.csv;

import com.ak.util.Extension;
import com.ak.util.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ak.csv.CSVLineFileBuilderTest.ROW_DELIMITER;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CSVLineFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVLineFileCollector.class.getName());
  private static final Path OUT_FILE;

  static {
    try {
      OUT_FILE = Files.createTempFile(
          "test %s ".formatted(CSVLineFileCollectorTest.class.getPackageName()),
          Extension.CSV.attachTo(CSVLineFileCollectorTest.class.getSimpleName())
      );
    }
    catch (IOException e) {
      fail(e);
      throw new RuntimeException(e);
    }
  }

  private static final AtomicInteger EXCEPTION_COUNTER = new AtomicInteger();

  @BeforeAll
  static void setUp() {
    LOGGER.setFilter(r -> {
      assertThat(r.getThrown()).isNotNull().isInstanceOf(IOException.class);
      EXCEPTION_COUNTER.incrementAndGet();
      return false;
    });
    LOGGER.setLevel(Level.WARNING);
  }

  @AfterAll
  static void tearDown() throws IOException {
    try {
      Files.deleteIfExists(OUT_FILE);
    }
    finally {
      LOGGER.setFilter(null);
      LOGGER.setLevel(Level.INFO);
    }
  }

  @BeforeEach
  void prepare() {
    EXCEPTION_COUNTER.set(0);
  }

  static Stream<Arguments> intStream() {
    return Stream.of(arguments((Supplier<Stream<String>>) () -> IntStream.rangeClosed(-1, 1).mapToObj("%d"::formatted)));
  }

  @ParameterizedTest
  @MethodSource("intStream")
  void testConsumer(Supplier<Stream<String>> stream) throws IOException {
    try (CSVLineFileCollector collector = new CSVLineFileCollector(OUT_FILE)) {
      collector.accept(stream.get().toArray(String[]::new));
    }
    assertThat(Files.readString(OUT_FILE, Charset.forName("windows-1251")).strip())
        .isEqualTo(stream.get().collect(Collectors.joining(ROW_DELIMITER)));
    assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must NOT be thrown").isZero();
  }

  @ParameterizedTest
  @MethodSource("intStream")
  void testVertical(Supplier<Stream<String>> stream) throws IOException {
    assertThat(stream.get().map(s -> new Object[] {s}).collect(new CSVLineFileCollector(OUT_FILE, "header"))).isTrue();
    assertThat(String.join(Strings.EMPTY, Files.readAllLines(OUT_FILE, Charset.forName("windows-1251"))))
        .isEqualTo(Stream.concat(Stream.of("header"), stream.get()).collect(Collectors.joining()));
    assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must NOT be thrown").isZero();
  }

  @ParameterizedTest
  @MethodSource("intStream")
  void testInvalidClose(Supplier<Stream<String>> stream) throws Throwable {
    CSVLineFileCollector collector = new CSVLineFileCollector(OUT_FILE);
    collector.close();
    assertThat(stream.get().map(s -> new Object[] {s}).collect(collector)).isTrue();
    assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must be thrown").isEqualTo(1);
    collector.close();
    assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must be thrown only once").isEqualTo(1);
  }

  static Stream<Arguments> invalidWriter() throws IOException {
    return Stream.of(
        arguments(
            new CSVPrinter(
                new BufferedWriter(
                    new Writer() {
                      @Override
                      public void write(char[] cBuf, int off, int len) {
                        fail();
                      }

                      @Override
                      public void flush() {
                        fail();
                      }

                      @Override
                      public void close() {
                        LOGGER.fine("close");
                      }
                    }),
                CSVFormat.DEFAULT
            )
        ));
  }

  @ParameterizedTest
  @MethodSource("invalidWriter")
  void testInvalidFinisher(CSVPrinter printer) {
    try (CSVLineFileCollector collector = new CSVLineFileCollector(OUT_FILE)) {
      collector.accumulator().accept(printer, new Object[] {Double.toString(Math.PI)});
      assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must NOT be thrown").isZero();
      collector.finisher().apply(printer);
    }
    catch (IOException | IllegalArgumentException e) {
      assertThat(e).hasMessageContaining("CSVPrinter");
    }
  }

  @Test
  void testCombiner() throws IOException {
    try (CSVLineFileCollector lineFileCollector = new CSVLineFileCollector(OUT_FILE)) {
      try (CSVPrinter apply = lineFileCollector.combiner().apply(lineFileCollector.supplier().get(), lineFileCollector.supplier().get())) {
        fail(apply.toString());
      }
    }
    catch (UnsupportedOperationException e) {
      assertThat(e.getMessage()).isNull();
    }
  }

  @Test
  void testInvalidPath() {
    Path out = Paths.get("/");
    assertThatNullPointerException()
        .isThrownBy(() -> {
          try (var _ = new CSVLineFileCollector(out)) {
            fail();
          }
        });
  }

  @Nested
  class Mocking {
    @Test
    void testTempFileNotCreated() throws IOException {
      try (MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
        mockFiles.when(() -> Files.createTempFile(any(), anyString(), anyString())).thenThrow(IOException.class);
        try (CSVLineFileCollector lineFileCollector = new CSVLineFileCollector(OUT_FILE)) {
          assertThatNoException().isThrownBy(lineFileCollector::close);
        }
      }
    }

    @ParameterizedTest
    @MethodSource("com.ak.csv.CSVLineFileCollectorTest#intStream")
    void testIOExceptionWhenClosed(Supplier<Stream<String>> stream) throws IOException {
      try (
          CSVLineFileCollector collector = new CSVLineFileCollector(OUT_FILE, "header");
          MockedStatic<Files> mockFiles = mockStatic(Files.class)
      ) {
        mockFiles.when(() -> Files.move(any(), any(), any(StandardCopyOption.class), any(StandardCopyOption.class)))
            .thenThrow(IOException.class);
        assertThat(stream.get().map(s -> new Object[] {s}).collect(collector)).isTrue();
      }
    }
  }
}