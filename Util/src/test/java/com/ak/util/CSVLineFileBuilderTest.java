package com.ak.util;

import java.io.IOException;
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
    new CSVLineFileBuilder<Double>()
        .xRange(1.0, 3.0, 1.0)
        .yRange(1.0, 2.0, 1.0)
        .saveTo("z", x -> x)
        .generate((x, y) -> x + y * 10);
    checkFilesExists("z", "\"\",1.0,2.0,3.0,1.0,11.0,12.0,13.0,2.0,21.0,22.0,23.0");
  }

  @Test
  public void testGenerateLogRange() throws IOException {
    new CSVLineFileBuilder<Double>()
        .xLog10Range(10.0, 20.0)
        .yLog10Range(100.0, 10.0)
        .saveTo("logZ", aDouble -> aDouble)
        .generate(Double::sum);
    checkFilesExists("logZ",
        "\"\",10.0,12.0,14.0,16.0,18.0,20.0,%s" .formatted(
            DoubleStream
                .iterate(10.0, value -> value <= 100.0, operand -> operand + 2.0)
                .flatMap(value -> DoubleStream.concat(
                    DoubleStream.of(value),
                    DoubleStream.iterate(10.0, d -> d <= 20.0, d -> d + 2.0).map(d -> d + value))
                )
                .map(value -> (int) Math.round(value))
                .mapToObj(Double::toString)
                .collect(Collectors.joining(Strings.COMMA))
        )
    );
  }

  @Test
  public void testGenerateStream() throws IOException {
    new CSVLineFileBuilder<Double>()
        .xStream(() -> DoubleStream.of(1.0, 2.0))
        .yStream(() -> DoubleStream.of(1.0, 2.0, 0.0))
        .saveTo("streamZ", x -> x)
        .generate((x, y) -> x + y * 2.0);
    checkFilesExists("streamZ", "\"\",1.0,2.0,1.0,3.0,4.0,2.0,5.0,6.0,0.0,1.0,2.0");
  }

  @ParametersAreNonnullByDefault
  private static void checkFilesExists(String fileName, String expected) throws IOException {
    Path z = Paths.get(Extension.CSV.attachTo(fileName));
    Assert.assertEquals(String.join(Strings.COMMA, Files.readAllLines(z, Charset.forName("windows-1251"))), expected);
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}