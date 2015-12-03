package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LineFileCollectorTest {
  private Path out;

  private LineFileCollectorTest() {
  }

  @BeforeClass
  public void setUp() {
    out = Paths.get(LineFileCollectorTest.class.getName() + ".txt");
  }

  @AfterClass
  public void tearDown() throws IOException {
    Files.deleteIfExists(out);
  }

  @DataProvider(name = "stream")
  public static Object[][] intStream() {
    return new Object[][] {
        {(Supplier<Stream<String>>) () -> IntStream.rangeClosed(-1, 1).mapToObj(value -> String.format("%d", value))}
    };
  }

  @Test(dataProvider = "stream")
  public void testVertical(Supplier<Stream<String>> stream) throws IOException {
    stream.get().collect(new LineFileCollector<>(out, LineFileCollector.Direction.VERTICAL));
    Assert.assertTrue(Files.readAllLines(out, Charset.forName("windows-1251")).stream().collect(Collectors.joining()).
        equals(stream.get().collect(Collectors.joining())));
  }

  @Test(dataProvider = "stream")
  public void testHorizontal(Supplier<Stream<String>> stream) throws IOException {
    stream.get().collect(new LineFileCollector<>(out, LineFileCollector.Direction.HORIZONTAL));
    Assert.assertTrue(Files.readAllLines(out, Charset.forName("windows-1251")).stream().collect(Collectors.joining()).
        equals(stream.get().collect(Collectors.joining("\t"))));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCombiner() {
    new LineFileCollector<>(out, LineFileCollector.Direction.HORIZONTAL).combiner().apply(null, null);
  }
}