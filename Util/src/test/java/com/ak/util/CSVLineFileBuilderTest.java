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
  public static final String LINE_JOINER = Strings.NEW_LINE;
  public static final String ROW_DELIMITER = ",";

  @Test
  public void testGenerateRange() throws IOException {
    CSVLineFileBuilder.of((x, y) -> x + y * 10)
        .xRange(1.0, 3.0, 1.0)
        .yRange(1.0, 2.0, 1.0)
        .saveTo("z", x -> x)
        .generate();
    checkFilesExists("z",
        String.join(LINE_JOINER,
            String.join(ROW_DELIMITER, "\"\"", "1.0", "2.0", "3.0"),
            String.join(ROW_DELIMITER, "1.0", "11.0", "12.0", "13.0"),
            String.join(ROW_DELIMITER, "2.0", "21.0", "22.0", "23.0")
        )
    );
  }

  @Test
  public void testGenerateLogRange() throws IOException {
    CSVLineFileBuilder.of(Double::sum)
        .xLog10Range(10.0, 20.0)
        .yLog10Range(10.0, 1.0)
        .saveTo("logZ", aDouble -> aDouble)
        .generate();
    checkFilesExists("logZ",
        String.join(LINE_JOINER,
            String.join(ROW_DELIMITER, "\"\"", "10.0", "12.0", "14.0", "16.0", "18.0", "20.0"),
            DoubleStream
                .iterate(1.0, value -> value <= 10.0, operand -> operand + 0.2)
                .mapToObj(value ->
                    DoubleStream
                        .concat(
                            DoubleStream.of(value),
                            DoubleStream.iterate(10.0, d -> d <= 20.0, d -> d + 2.0).map(d -> d + value))
                        .map(x -> BigDecimal.valueOf(x).setScale(1, RoundingMode.HALF_EVEN).doubleValue())
                        .mapToObj(Double::toString).collect(Collectors.joining(ROW_DELIMITER))
                )
                .collect(Collectors.joining(LINE_JOINER))
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
    checkFilesExists("streamZ",
        String.join(LINE_JOINER,
            String.join(ROW_DELIMITER, "\"\"", "1.0", "2.0"),
            String.join(ROW_DELIMITER, "1.0", "3.0", "4.0"),
            String.join(ROW_DELIMITER, "2.0", "5.0", "6.0"),
            String.join(ROW_DELIMITER, "0.0", "1.0", "2.0")
        )
    );
  }

  @ParametersAreNonnullByDefault
  private static void checkFilesExists(String fileName, String expected) throws IOException {
    Path z = Paths.get(Extension.CSV.attachTo(fileName));
    Assert.assertEquals(String.join(LINE_JOINER, Files.readAllLines(z, Charset.forName("windows-1251"))), expected);
    Assert.assertTrue(Files.deleteIfExists(z));
  }
}