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

import javax.annotation.Nonnull;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CSVLineFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVLineFileCollector.class.getName());
  private static final String OUT = CSVLineFileCollectorTest.class.getName();
  private final AtomicInteger exceptionCounter = new AtomicInteger();
  private Path out;

  @BeforeClass
  public void setUp() {
    out = Paths.get(Extension.CSV.attachTo(CSVLineFileCollectorTest.class.getName()));
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

  @DataProvider(name = "stream")
  public static Object[][] intStream() {
    return new Object[][] {
        {(Supplier<Stream<String>>) () -> IntStream.rangeClosed(-1, 1).mapToObj("%d"::formatted)}
    };
  }

  @Test(dataProvider = "stream")
  public void testConsumer(@Nonnull Supplier<Stream<String>> stream) throws IOException {
    try (CSVLineFileCollector collector = new CSVLineFileCollector(OUT)) {
      collector.accept(stream.get().toArray(String[]::new));
    }
    Assert.assertEquals(Files.readString(out, Charset.forName("windows-1251")).trim(),
        stream.get().collect(Collectors.joining(",")));
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
  }

  @Test(dataProvider = "stream")
  public void testVertical(@Nonnull Supplier<Stream<String>> stream) throws IOException {
    Assert.assertTrue(stream.get().collect(new CSVLineFileCollector(OUT, "header")));
    Assert.assertEquals(String.join("", Files.readAllLines(out, Charset.forName("windows-1251"))),
        Stream.concat(Stream.of("header"), stream.get()).collect(Collectors.joining()));
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
  }

  @Test(dataProvider = "stream")
  public void testInvalidClose(@Nonnull Supplier<Stream<String>> stream) throws Throwable {
    CSVLineFileCollector collector = new CSVLineFileCollector(OUT);
    collector.close();
    Assert.assertFalse(stream.get().collect(collector));
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown");
    collector.close();
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must be thrown only once");
  }

  @DataProvider(name = "invalid-writer")
  public static Object[][] writer() throws IOException {
    return new Object[][] {
        {
            new CSVPrinter(
                new BufferedWriter(
                    new Writer() {
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
                    }),
                CSVFormat.DEFAULT
            )
        }
    };
  }

  @Test(dataProvider = "invalid-writer")
  public void testInvalidFinisher(@Nonnull CSVPrinter printer) {
    Collector<Object, CSVPrinter, Boolean> collector = new CSVLineFileCollector(OUT);
    collector.accumulator().accept(printer, Double.toString(Math.PI));
    Assert.assertEquals(exceptionCounter.get(), 0, "Exception must NOT be thrown");
    collector.finisher().apply(printer);
    Assert.assertEquals(exceptionCounter.get(), 1, "Exception must NOT be thrown");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCombiner() {
    new CSVLineFileCollector(OUT).combiner().apply(null, null);
  }

  @Test
  public void testInvalidPath() {
    new CSVLineFileCollector("/");
  }
}