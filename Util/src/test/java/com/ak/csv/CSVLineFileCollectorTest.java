package com.ak.csv;

import com.ak.util.Extension;
import com.ak.util.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ak.csv.CSVLineFileBuilderTest.ROW_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CSVLineFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVLineFileCollector.class.getName());
  private static final Path OUT_PATH = Paths.get(Extension.CSV.attachTo(CSVLineFileCollectorTest.class.getName()));
  private static final AtomicInteger EXCEPTION_COUNTER = new AtomicInteger();

  @BeforeAll
  static void setUp() {
    LOGGER.setFilter(record -> {
      assertNotNull(record.getThrown());
      EXCEPTION_COUNTER.incrementAndGet();
      return false;
    });
    LOGGER.setLevel(Level.WARNING);
  }

  @AfterAll
  static void tearDown() throws IOException {
    try {
      Files.deleteIfExists(OUT_PATH);
    }
    finally {
      LOGGER.setFilter(null);
      LOGGER.setLevel(Level.INFO);
    }
  }

  @BeforeEach
  public void prepare() {
    EXCEPTION_COUNTER.set(0);
  }

  static Stream<Arguments> intStream() {
    return Stream.of(arguments((Supplier<Stream<String>>) () -> IntStream.rangeClosed(-1, 1).mapToObj("%d"::formatted)));
  }

  @ParameterizedTest
  @MethodSource("intStream")
  void testConsumer(Supplier<Stream<String>> stream) throws IOException {
    try (CSVLineFileCollector collector = new CSVLineFileCollector(OUT_PATH)) {
      collector.accept(stream.get().toArray(String[]::new));
    }
    assertThat(Files.readString(OUT_PATH, Charset.forName("windows-1251")).trim())
        .isEqualTo(stream.get().collect(Collectors.joining(ROW_DELIMITER)));
    assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must NOT be thrown").isZero();
  }

  @ParameterizedTest
  @MethodSource("intStream")
  void testVertical(Supplier<Stream<String>> stream) throws IOException {
    assertTrue(stream.get().map(s -> new Object[] {s}).collect(new CSVLineFileCollector(OUT_PATH, "header")));
    assertThat(String.join(Strings.EMPTY, Files.readAllLines(OUT_PATH, Charset.forName("windows-1251"))))
        .isEqualTo(Stream.concat(Stream.of("header"), stream.get()).collect(Collectors.joining()));
    assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must NOT be thrown").isZero();
  }

  @ParameterizedTest
  @MethodSource("intStream")
  void testInvalidClose(Supplier<Stream<String>> stream) throws Throwable {
    CSVLineFileCollector collector = new CSVLineFileCollector(OUT_PATH);
    collector.close();
    assertTrue(stream.get().map(s -> new Object[] {s}).collect(collector));
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
                      }

                      @Override
                      public void flush() {
                      }

                      @Override
                      public void close() {
                      }
                    }),
                CSVFormat.DEFAULT
            )
        ));
  }

  @ParameterizedTest
  @MethodSource("invalidWriter")
  void testInvalidFinisher(CSVPrinter printer) {
    try (CSVLineFileCollector collector = new CSVLineFileCollector(OUT_PATH)) {
      collector.accumulator().accept(printer, new Object[] {Double.toString(Math.PI)});
      assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must NOT be thrown").isZero();
      collector.finisher().apply(printer);
    }
    catch (IOException | IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("CSVPrinter"));
    }
  }

  @Test
  void testCombiner() throws IOException {
    try (CSVLineFileCollector lineFileCollector = new CSVLineFileCollector(OUT_PATH)) {
      try (CSVPrinter apply = lineFileCollector.combiner().apply(null, null)) {
        fail(apply.toString());
      }
    }
    catch (UnsupportedOperationException e) {
      assertNull(e.getMessage());
    }
  }

  @Test
  void testInvalidPath() {
    Path out = Paths.get("/");
    assertThatNullPointerException()
        .isThrownBy(() -> {
          try (var ignored = new CSVLineFileCollector(out)) {
            fail();
          }
        });
  }
}