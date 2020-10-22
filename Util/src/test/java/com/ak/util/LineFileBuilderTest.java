package com.ak.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LineFileBuilderTest {
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "1 invalid")
  public void testOf() {
    LineFileBuilder.of("%.4f %.2f %.1f");
    LineFileBuilder.of("1 invalid");
  }

  @Test
  public void testGenerateRange() throws IOException {
    LineFileBuilder.of("%.1f %.2f %.3f")
        .xRange(1.0, 3.0, 1.0)
        .yRange(1.0, 2.0, 1.0)
        .generate("z.txt", (x, y) -> x + y * 10);
    checkFilesExists(
        DoubleStream.of(11, 12, 13, 21, 22, 23).mapToObj(n -> String.format("%.3f", n)).collect(Collectors.joining(Strings.TAB))
    );

    LineFileBuilder.<Double>of("%.1f %.2f %.3f")
        .xRange(1.0, 3.0, 1.0)
        .yRange(1.0, 2.0, 1.0)
        .add("z.txt", value -> value)
        .generate((x, y) -> x + y * 10);
    checkFilesExists(
        DoubleStream.of(11, 12, 13, 21, 22, 23).mapToObj(n -> String.format("%.3f", n)).collect(Collectors.joining(Strings.TAB))
    );

    LineFileBuilder.of("%.1f %.2f %.3f")
        .xRange(1.0, 3.0, 1.0)
        .yRange(1.0, 2.0, 1.0)
        .generateR("z.txt", (x, y) -> x + y * 10);
    checkFilesExists(
        "\"\",\"1.0\",\"2.0\",\"3.0\"\t\"1.00\",11.000,12.000,13.000\t\"2.00\",21.000,22.000,23.000"
    );
  }

  private static void checkFilesExists(@Nonnull String expected) throws IOException {
    Path x = Paths.get("x.txt");
    Assert.assertEquals(String.join("", Files.readAllLines(x, Charset.forName("windows-1251"))),
        DoubleStream.of(1, 2, 3).mapToObj(n -> String.format("%.1f", n)).collect(Collectors.joining(Strings.TAB)));
    Assert.assertTrue(Files.deleteIfExists(x));

    Path y = Paths.get("y.txt");
    Assert.assertEquals(String.join(Strings.SPACE, Files.readAllLines(y, Charset.forName("windows-1251"))),
        DoubleStream.of(1, 2).mapToObj(n -> String.format("%.2f", n)).collect(Collectors.joining(Strings.SPACE)));
    Assert.assertTrue(Files.deleteIfExists(y));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))), expected);
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
    LineFileBuilder.of("%.0f %.0f %.1f")
        .xStream(() -> DoubleStream.of(1.0, 2.0))
        .yStream(() -> DoubleStream.of(1.0, 2.0))
        .generate("z.txt", (x, y) -> x + y * 2.0);

    Assert.assertTrue(Files.notExists(Paths.get("x.txt")));
    Assert.assertTrue(Files.notExists(Paths.get("y.txt")));

    Path z = Paths.get("z.txt");
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))),
        DoubleStream.of(3, 4, 5, 6).mapToObj(n -> String.format("%.1f", n)).collect(Collectors.joining(Strings.TAB)));
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}