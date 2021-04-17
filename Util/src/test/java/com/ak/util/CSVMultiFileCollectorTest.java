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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CSVMultiFileCollectorTest {
  private static final Logger LOGGER = Logger.getLogger(CSVMultiFileCollectorTest.class.getName());
  private static final String OUT_FILE_NAME = CSVMultiFileCollectorTest.class.getSimpleName();
  private static final Path OUT_PATH = Paths.get(Extension.TXT.attachTo(OUT_FILE_NAME));
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
    CSVMultiFileCollector<Integer, Double> multiFileCollector = new CSVMultiFileCollector.Builder<Integer, Double>(
        IntStream.of(1, 2).boxed(), "var1", "var2").
        add(OUT_FILE_NAME, value -> value).build();
    Assert.assertTrue(Stream.of(Stream.of(1.0, 1.1), Stream.of(2.0, 2.1)).collect(multiFileCollector));
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(OUT_PATH, Charset.forName("windows-1251"))),
        "var1\tvar2\t1\t1.0\t1.1\t2\t2.0\t2.1");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidCombiner() {
    new CSVMultiFileCollector.Builder<Object, Double>(Stream.empty()).build().combiner().apply(null, null);
  }
}