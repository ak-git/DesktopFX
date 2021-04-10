package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CSVMultiFileCollectorTest {
  @Test
  public void test() throws IOException {
    String out = CSVMultiFileCollectorTest.class.getSimpleName();
    CSVMultiFileCollector<Double> multiFileCollector = new CSVMultiFileCollector.Builder<Double>().
        add(out, value -> value).build();
    Assert.assertTrue(Stream.generate(() -> Stream.of(1.0, 2.0)).limit(1).collect(multiFileCollector));

    Path path = Paths.get(Extension.CSV.attachTo(out));
    Assert.assertEquals(Files.readString(path, Charset.forName("windows-1251")).trim(), "1.0,2.0");
    Assert.assertTrue(Files.deleteIfExists(path));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidCombiner() {
    new CSVMultiFileCollector.Builder<Double>().build().combiner().apply(null, null);
  }
}