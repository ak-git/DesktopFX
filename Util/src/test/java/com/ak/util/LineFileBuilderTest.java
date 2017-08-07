package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LineFileBuilderTest {
  private LineFileBuilderTest() {
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "1 invalid")
  public static void testOf() {
    LineFileBuilder.of("%.4f %.2f %.1f");
    LineFileBuilder.of("1 invalid");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testXRange() {
    LineFileBuilder.of("%.2f %.2f %.1f").xRange(2.0, 1.0, 0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testYRange() {
    LineFileBuilder.of("%.2f %.2f %.1f").yRange(1.0, 10.0, 10.0);
  }

  @Test
  public static void testGenerateRange() throws IOException {
    LineFileBuilder.of("%.0f %.0f %.0f").
        xRange(1.0, 3.0, 1.0).
        yRange(1.0, 2.0, 1.0).generate((x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(Files.readAllLines(x, Charset.forName("windows-1251")).stream().collect(Collectors.joining()),
        "1\t2\t3");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(Files.readAllLines(y, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.SPACE)),
        "1 2");
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(Files.readAllLines(z, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "11\t12\t13\t21\t22\t23");
    Assert.assertTrue(Files.deleteIfExists(z));
  }

  @Test
  public static void testGenerateStream() throws IOException {
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> DoubleStream.of(1.0, 2.0)).
        yStream(() -> DoubleStream.of(1.0, 2.0)).generate((x, y) -> x + y * 2.0);

    Assert.assertTrue(Files.notExists(Paths.get("x.txt")));
    Assert.assertTrue(Files.notExists(Paths.get("y.txt")));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(Files.readAllLines(z, Charset.forName("windows-1251")).stream().collect(Collectors.joining(Strings.TAB)),
        "3\t4\t5\t6");
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}