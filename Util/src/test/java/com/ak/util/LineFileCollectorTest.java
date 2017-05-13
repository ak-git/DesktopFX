package com.ak.util;

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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javafx.geometry.Orientation;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LineFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(LineFileCollector.class.getName());
  private final AtomicInteger exceptionCounter = new AtomicInteger();
  private Path out;

  private LineFileCollectorTest() {
  }

  @BeforeClass
  public void setUp() {
    out = Paths.get(LineFileCollectorTest.class.getName() + ".txt");

    LOGGER.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      exceptionCounter.incrementAndGet();
      return false;
    });
    LOGGER.setLevel(Level.WARNING);
  }

  @AfterClass
  public void tearDown() throws IOException {
    Files.deleteIfExists(out);

    LOGGER.setFilter(null);
    LOGGER.setLevel(Level.INFO);
  }

  @BeforeMethod
  public void prepare() {
    exceptionCounter.set(0);
  }

  @Test
  public static void testDirectionNames() {
    for (Orientation orientation : Orientation.values()) {
      LineFileCollector.Direction.valueOf(orientation.name());
    }
    for (LineFileCollector.Direction direction : LineFileCollector.Direction.values()) {
      Orientation.valueOf(direction.name());
    }
  }

  @DataProvider(name = "stream")
  public static Object[][] intStream() {
    return new Object[][] {
        {(Supplier<Stream<String>>) () -> IntStream.rangeClosed(-1, 1).mapToObj(value -> String.format("%d", value))}
    };
  }

  @Test(dataProvider = "stream")
  public void testConsumer(Supplier<Stream<String>> stream) throws IOException {
    try (LineFileCollector collector = new LineFileCollector(out, LineFileCollector.Direction.VERTICAL)) {
      stream.get().forEach(collector);
    }
    Assert.assertTrue(Files.readAllLines(out, Charset.forName("windows-1251")).stream().collect(Collectors.joining()).
        equals(stream.get().collect(Collectors.joining())));
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
  }

  @Test(dataProvider = "stream")
  public void testVertical(Supplier<Stream<String>> stream) throws IOException {
    Assert.assertNull(stream.get().collect(new LineFileCollector(out, LineFileCollector.Direction.VERTICAL)));
    Assert.assertTrue(Files.readAllLines(out, Charset.forName("windows-1251")).stream().collect(Collectors.joining()).
        equals(stream.get().collect(Collectors.joining())));
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
  }

  @Test(dataProvider = "stream")
  public void testHorizontal(Supplier<Stream<String>> stream) throws IOException {
    Assert.assertNull(stream.get().collect(new LineFileCollector(out, LineFileCollector.Direction.HORIZONTAL)));
    Assert.assertTrue(Files.readAllLines(out, Charset.forName("windows-1251")).stream().collect(Collectors.joining()).
        equals(stream.get().collect(Collectors.joining(Strings.TAB))));
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
  }

  @Test(dataProvider = "stream")
  public void testInvalidClose(Supplier<Stream<String>> stream) throws Throwable {
    LineFileCollector collector = new LineFileCollector(out, LineFileCollector.Direction.VERTICAL);
    collector.close();
    Assert.assertNull(stream.get().collect(collector));
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown");
    collector.close();
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown only once");
  }

  @DataProvider(name = "invalid-writer")
  public static Object[][] writer() {
    return new Object[][] {
        {
            new BufferedWriter(new Writer() {
              @Override
              public void write(char[] cBuf, int off, int len) {
              }

              @Override
              public void flush() throws IOException {
                throw new IOException(getClass().getSimpleName());
              }

              @Override
              public void close() {
              }
            })
        }
    };
  }

  @Test(dataProvider = "invalid-writer")
  public void testInvalidAccumulator(BufferedWriter bufferedWriter) throws IOException {
    Collector<Object, BufferedWriter, Void> collector = new LineFileCollector(out, LineFileCollector.Direction.VERTICAL);
    collector.accumulator().accept(bufferedWriter, Math.PI);
    bufferedWriter.close();
    collector.accumulator().accept(bufferedWriter, Math.PI);

    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown");
    collector.finisher().apply(bufferedWriter);
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown only once");
  }

  @Test(dataProvider = "invalid-writer")
  public void testInvalidFinisher(BufferedWriter bufferedWriter) throws IOException {
    Collector<Object, BufferedWriter, Void> collector = new LineFileCollector(out, LineFileCollector.Direction.VERTICAL);
    collector.accumulator().accept(bufferedWriter, Math.PI);
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
    collector.finisher().apply(bufferedWriter);
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCombiner() throws IOException {
    new LineFileCollector(out, LineFileCollector.Direction.HORIZONTAL).combiner().apply(null, null);
  }
}