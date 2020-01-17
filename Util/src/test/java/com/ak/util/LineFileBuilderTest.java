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
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "1 invalid")
  public void testOf() {
    LineFileBuilder.of("%.4f %.2f %.1f");
    LineFileBuilder.of("1 invalid");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testXRange() {
    LineFileBuilder.of("%.2f %.2f %.1f").xRange(2.0, 1.0, 0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testYRange() {
    LineFileBuilder.of("%.2f %.2f %.1f").yRange(1.0, 10.0, 10.0);
  }

  @Test
  public void testGenerateRange() throws IOException {
    LineFileBuilder.of("%.0f %.0f %.0f").
        xRange(1.0, 3.0, 1.0).
        yRange(1.0, 2.0, 1.0).generate("z.txt", (x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(String.join("", Files.readAllLines(x, Charset.forName("windows-1251"))),
        "1\t2\t3");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(String.join(Strings.SPACE, Files.readAllLines(y, Charset.forName("windows-1251"))),
        "1 2");
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))),
        "11\t12\t13\t21\t22\t23");
    Assert.assertTrue(Files.deleteIfExists(z));
  }

  @Test
  public void testGenerateRange2() throws IOException {
    LineFileBuilder.<Double>of("%.0f %.0f %.0f").
        xRange(1.0, 3.0, 1.0).
        yRange(1.0, 2.0, 1.0).
        add("z.txt", value -> value).generate((x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(String.join("", Files.readAllLines(x, Charset.forName("windows-1251"))),
        "1\t2\t3");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(String.join(Strings.SPACE, Files.readAllLines(y, Charset.forName("windows-1251"))),
        "1 2");
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))),
        "11\t12\t13\t21\t22\t23");
    Assert.assertTrue(Files.deleteIfExists(z));
  }

  @Test
  public void testGenerateLogRange() throws IOException {
    LineFileBuilder.of("%.0f %.1f %.0f").
        xLog10Range(10.0, 20.0).
        yLog10Range(10.0, 1.0).generate("z.txt", (x, y) -> x + y * 10);

    Path x = Paths.get("x.txt");
    Assert.assertEquals(String.join("", Files.readAllLines(x, Charset.forName("windows-1251"))),
        "10\t12\t14\t16\t18\t20");
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(String.join(Strings.SPACE, Files.readAllLines(y, Charset.forName("windows-1251"))),
        DoubleStream.iterate(1.0, d -> d <= 10.0, d -> d + 0.2)
            .mapToObj(d -> String.format("%.1f", d)).collect(Collectors.joining(Strings.SPACE)));
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))),
        DoubleStream.iterate(1.0, value -> value <= 10.0, operand -> operand + 0.2)
            .flatMap(value -> DoubleStream.iterate(10.0, d -> d <= 20.0, d -> d + 2).map(d -> d + value * 10))
            .mapToObj(value -> String.format("%.0f", value)).collect(Collectors.joining(Strings.TAB)));
    Assert.assertTrue(Files.deleteIfExists(z));
  }

  @Test
  public void testGenerateStream() throws IOException {
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> DoubleStream.of(1.0, 2.0)).
        yStream(() -> DoubleStream.of(1.0, 2.0)).generate("z.txt", (x, y) -> x + y * 2.0);

    Assert.assertTrue(Files.notExists(Paths.get("x.txt")));
    Assert.assertTrue(Files.notExists(Paths.get("y.txt")));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))),
        "3\t4\t5\t6");
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}