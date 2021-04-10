package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CSVMultiFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVMultiFileCollectorTest.class.getName());
  private static final String OUT_FILE_NAME = CSVMultiFileCollectorTest.class.getSimpleName();
  private static final Path OUT_PATH = Paths.get(Extension.CSV.attachTo(OUT_FILE_NAME));
  private final AtomicInteger exceptionCounter = new AtomicInteger();

  @BeforeClass
  public void setUp() {
    LOGGER.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      exceptionCounter.incrementAndGet();
      return false;
    });
    LOGGER.setLevel(Level.WARNING);
  }

  @AfterClass
  public void tearDown() throws IOException {
    try {
      Files.deleteIfExists(OUT_PATH);
    }
    finally {
      LOGGER.setFilter(null);
      LOGGER.setLevel(Level.INFO);
    }
  }

  @BeforeMethod
  public void prepare() {
    exceptionCounter.set(0);
  }

  @Test
  public void test() throws IOException {
    CSVMultiFileCollector<Double> multiFileCollector = new CSVMultiFileCollector.Builder<Double>().
        add(OUT_FILE_NAME, value -> value).build();
    Assert.assertTrue(Stream.generate(() -> Stream.of(1.0, 2.0)).limit(1).collect(multiFileCollector));

    Assert.assertEquals(Files.readString(OUT_PATH, Charset.forName("windows-1251")).trim(), "1.0,2.0");
    Assert.assertTrue(Files.deleteIfExists(OUT_PATH));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidCombiner() {
    new CSVMultiFileCollector.Builder<Double>().build().combiner().apply(null, null);
  }
}