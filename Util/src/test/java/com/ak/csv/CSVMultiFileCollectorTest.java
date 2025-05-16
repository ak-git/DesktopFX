package com.ak.csv;

import com.ak.util.Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ak.csv.CSVLineFileBuilderTest.LINE_JOINER;
import static com.ak.csv.CSVLineFileBuilderTest.ROW_DELIMITER;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CSVMultiFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVMultiFileCollector.class.getName());
  private static final Path OUT_FILE;

  static {
    try {
      OUT_FILE = Files.createTempFile(
          "test %s ".formatted(CSVMultiFileCollectorTest.class.getPackageName()),
          Extension.CSV.attachTo(CSVMultiFileCollectorTest.class.getSimpleName())
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

  @Test
  void test() throws IOException {
    CSVMultiFileCollector<Integer, Double> multiFileCollector = new CSVMultiFileCollector.Builder<Integer, Double>(
        IntStream.of(1, 2).boxed(), "var1", "var2").
        add(OUT_FILE, value -> value).build();
    assertTrue(Stream.of(Stream.of(1.0, 1.1), Stream.of(2.0, 2.1)).collect(multiFileCollector));
    assertThat(String.join(LINE_JOINER, Files.readAllLines(OUT_FILE, Charset.forName("windows-1251"))))
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

  @Nested
  class Mocking {
    @Mock
    private CSVLineFileCollector csvLineFileCollector;

    @Test
    void testInvalidFinisher() throws IOException {
      var finisher = new CSVMultiFileCollector.Builder<Object, Double>(Stream.empty()).build().finisher();
      Mockito.doThrow(IOException.class).when(csvLineFileCollector).close();
      assertThat(finisher.apply(List.of(csvLineFileCollector))).isFalse();
      Mockito.verify(csvLineFileCollector).close();
      assertThat(EXCEPTION_COUNTER.get()).withFailMessage("Exception must be thrown").isEqualTo(1);
    }
  }
}