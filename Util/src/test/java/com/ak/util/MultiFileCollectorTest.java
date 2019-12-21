package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MultiFileCollectorTest {
  @Test
  public void test() throws IOException {
    Path out = Paths.get(MultiFileCollectorTest.class.getSimpleName() + ".txt");
    MultiFileCollector<Double> multiFileCollector = new MultiFileCollector.MultiFileCollectorBuilder<Double>("%.0f").
        add(out, value -> value).build();
    Assert.assertTrue(Stream.generate(() -> Stream.of(1.0, 2.0)).limit(1).collect(multiFileCollector));

    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(out, Charset.forName("windows-1251"))),
        "1\t2");
    Assert.assertTrue(Files.deleteIfExists(out));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testInvalidCombiner() {
    new MultiFileCollector.MultiFileCollectorBuilder<Double>("%.2f").build().combiner().apply(null, null);
  }
}