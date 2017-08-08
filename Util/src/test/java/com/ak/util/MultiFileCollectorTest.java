package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MultiFileCollectorTest {
  @Test
  public void test() throws IOException {
    Path out = Paths.get(MultiFileCollectorTest.class.getSimpleName() + ".txt");
    MultiFileCollector<Double> multiFileCollector = new MultiFileCollector.Builder<Double>("%.1f").
        add(out, value -> value).build();
    Assert.assertTrue(Stream.generate(() -> Stream.of(1.0, 2.0)).limit(1).collect(multiFileCollector));

    Assert.assertEquals(Files.readAllLines(out, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "1,0\t2,0");
    Assert.assertTrue(Files.deleteIfExists(out));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidCombiner() {
    new MultiFileCollector.Builder<Double>("%.2f").build().combiner().apply(null, null);
  }
}