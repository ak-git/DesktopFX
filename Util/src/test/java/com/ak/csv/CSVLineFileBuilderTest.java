package com.ak.csv;

import com.ak.util.Extension;
import com.ak.util.Strings;
import org.junit.jupiter.api.Test;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CSVLineFileBuilderTest {
  public static final String LINE_JOINER = Strings.NEW_LINE;
  public static final String ROW_DELIMITER = ",";

  @Test
  void testGenerateRange() throws IOException {
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
  void testGenerateLogRange() throws IOException {
    DoubleUnaryOperator round = x -> BigDecimal.valueOf(x).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    CSVLineFileBuilder.of(Double::sum)
        .xLogRange(10.0, 20.0)
        .yLogRange(10.0, 1.0)
        .saveTo("logZ", round::applyAsDouble)
        .generate();
    checkFilesExists("logZ",
        String.join(LINE_JOINER,
            String.join(ROW_DELIMITER, "\"\"", "10.0", "10.8", "11.7", "12.6", "13.6", "14.7", "15.9", "17.1", "18.5", "20.0"),
            DoubleStream
                .of(1.0, 1.29, 1.67, 2.15, 2.8, 3.6, 4.6, 6.0, 7.7, 10.0)
                .mapToObj(value ->
                    DoubleStream
                        .concat(
                            DoubleStream.of(value),
                            DoubleStream.of(10.0, 10.8, 11.7, 12.6, 13.6, 14.7, 15.9, 17.1, 18.5, 20.0).map(d -> d + value))
                        .map(round)
                        .mapToObj(Double::toString).collect(Collectors.joining(ROW_DELIMITER))
                )
                .collect(Collectors.joining(LINE_JOINER))
        )
    );
  }

  @Test
  void testGenerateStream() throws IOException {
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
    assertThat(String.join(LINE_JOINER, Files.readAllLines(z, Charset.forName("windows-1251")))).isEqualTo(expected);
    assertTrue(Files.deleteIfExists(z));
  }
}