package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ak.util.CSVLineFileBuilderTest.LINE_JOINER;
import static com.ak.util.CSVLineFileBuilderTest.ROW_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CSVMultiFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVMultiFileCollectorTest.class.getName());
  private static final Path OUT_PATH = Paths.get(Extension.CSV.attachTo(CSVMultiFileCollectorTest.class.getSimpleName()));
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

  @Test
  void test() throws IOException {
    CSVMultiFileCollector<Integer, Double> multiFileCollector = new CSVMultiFileCollector.Builder<Integer, Double>(
        IntStream.of(1, 2).boxed(), "var1", "var2").
        add(OUT_PATH, value -> value).build();
    assertTrue(Stream.of(Stream.of(1.0, 1.1), Stream.of(2.0, 2.1)).collect(multiFileCollector));
    assertThat(String.join(LINE_JOINER, Files.readAllLines(OUT_PATH, Charset.forName("windows-1251"))))
        .isEqualTo(String.join(LINE_JOINER,
                String.join(ROW_DELIMITER, "var1", "var2"),
                String.join(ROW_DELIMITER, "1", "1.0", "1.1"),
                String.join(ROW_DELIMITER, "2", "2.0", "2.1")
            )
        );
  }

  @Test
  void testInvalidCombiner() {
    var combiner = new CSVMultiFileCollector.Builder<Object, Double>(Stream.empty()).build().combiner();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> combiner.apply(null, null));
  }
}