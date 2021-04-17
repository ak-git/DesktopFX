package com.ak.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CSVLineFileBuilderTest {
  @Test
  public void testGenerateRange() throws IOException {
    CSVLineFileBuilder.of((x, y) -> x + y * 10)
        .xRange(1.0, 3.0, 1.0)
        .yRange(1.0, 2.0, 1.0)
        .saveTo("z", x -> x)
        .generate();
    checkFilesExists("z", "\"\"\t1.0\t2.0\t3.0\t1.0\t11.0\t12.0\t13.0\t2.0\t21.0\t22.0\t23.0");
  }

  @Test
  public void testGenerateLogRange() throws IOException {
    CSVLineFileBuilder.of(Double::sum)
        .xLog10Range(10.0, 20.0)
        .yLog10Range(10.0, 1.0)
        .saveTo("logZ", aDouble -> aDouble)
        .generate();
    checkFilesExists("logZ",
        "\"\"\t10.0\t12.0\t14.0\t16.0\t18.0\t20.0\t%s".formatted(
            DoubleStream
                .iterate(1.0, value -> value <= 10.0, operand -> operand + 0.2)
                .flatMap(value -> DoubleStream.concat(
                    DoubleStream.of(value),
                    DoubleStream.iterate(10.0, d -> d <= 20.0, d -> d + 2.0).map(d -> d + value))
                )
                .map(value -> BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_EVEN).doubleValue())
                .mapToObj(Double::toString)
                .collect(Collectors.joining(Strings.TAB))
        )
    );
  }

  @Test
  public void testGenerateStream() throws IOException {
    CSVLineFileBuilder.of((x, y) -> x + y * 2.0)
        .xStream(() -> DoubleStream.of(1.0, 2.0))
        .yStream(() -> DoubleStream.of(1.0, 2.0, 0.0))
        .saveTo("streamZ", x -> x)
        .generate();
    checkFilesExists("streamZ", "\"\"\t1.0\t2.0\t1.0\t3.0\t4.0\t2.0\t5.0\t6.0\t0.0\t1.0\t2.0");
  }

  @ParametersAreNonnullByDefault
  private static void checkFilesExists(String fileName, String expected) throws IOException {
    Path z = Paths.get(Extension.TXT.attachTo(fileName));
    Assert.assertEquals(String.join(Strings.TAB, Files.readAllLines(z, Charset.forName("windows-1251"))), expected);
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}