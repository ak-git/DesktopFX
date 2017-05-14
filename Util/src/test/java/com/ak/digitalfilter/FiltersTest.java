package com.ak.digitalfilter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.function.IntBinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FiltersTest {
  private FiltersTest() {
  }

  @DataProvider(name = "hypot")
  public static Object[][] hypot() {
    return new Object[][] {{
        1, (IntBinaryOperator) Filters::hypot02, 1, 1
    }, {
        10, (IntBinaryOperator) Filters::hypot02, 10, 14
    }, {
        100, (IntBinaryOperator) Filters::hypot02, 100, 141
    }, {
        1000, (IntBinaryOperator) Filters::hypot02, 1000, 1406
    }, {
        10000, (IntBinaryOperator) Filters::hypot02, 10000, 14063
    }, {
        1, (IntBinaryOperator) Filters::hypot63, 1, 1
    }, {
        10, (IntBinaryOperator) Filters::hypot63, 10, 15
    }, {
        100, (IntBinaryOperator) Filters::hypot63, 94, 141
    }};
  }

  @Test(dataProvider = "hypot")
  public static void testHypot(@Nonnegative int a, @Nonnull IntBinaryOperator hOperator, @Nonnegative int min, @Nonnegative int max) {
    IntSummaryStatistics stat = IntStream.range(0, a + 1).map(b -> hOperator.applyAsInt(a, b)).summaryStatistics();
    Assert.assertEquals(stat.getMin(), min, Integer.toString(stat.getMin()));
    Assert.assertEquals(stat.getMax(), max, Integer.toString(stat.getMax()));
  }

  @DataProvider(name = "cathetus")
  public static Object[][] cathetus() {
    return new Object[][] {{
        1, (IntBinaryOperator) Filters::cathetus63, 0, 1
    }, {
        10, (IntBinaryOperator) Filters::cathetus63, 0, 10
    }, {
        100, (IntBinaryOperator) Filters::cathetus63, 12, 106
    }, {
        1000, (IntBinaryOperator) Filters::cathetus63, 124, 1062
    }, {
        10000, (IntBinaryOperator) Filters::cathetus63, 1250, 10625
    }};
  }

  @Test(dataProvider = "cathetus")
  public static void testCathetus(@Nonnegative int c, @Nonnull IntBinaryOperator cathetusOperator, @Nonnegative int min, @Nonnegative int max) {
    IntSummaryStatistics stat = IntStream.range(0, c + 1).map(b -> cathetusOperator.applyAsInt(b, c)).summaryStatistics();
    Assert.assertEquals(stat.getMin(), min, Integer.toString(stat.getMin()));
    Assert.assertEquals(stat.getMax(), max, Integer.toString(stat.getMax()));
  }

  @Test(enabled = false)
  public static void textFiles() throws IOException {
    String filteredPrefix = "Filtered - ";
    int column = 0;

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Strings.EMPTY), "*.txt")) {
      directoryStream.forEach(path -> {
        if (!path.toString().startsWith(filteredPrefix)) {
          DigitalFilter filter = FilterBuilder.of().smoothingImpulsive(10).build();

          try (LineFileCollector collector = new LineFileCollector(
              Paths.get(String.format("%s%s", filteredPrefix, path.getFileName().toString())), LineFileCollector.Direction.VERTICAL)) {
            filter.forEach(values ->
                collector.accept(Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(Strings.TAB))));

            try (Stream<String> lines = Files.lines(path, Charset.forName("windows-1251"))) {
              lines.filter(s -> s.matches("\\d+.*")).mapToInt(value -> {
                try {
                  return NumberFormat.getIntegerInstance().parse(value.split("\\t")[column]).intValue();
                }
                catch (ParseException e) {
                  Logger.getLogger(FiltersTest.class.getName()).log(Level.INFO, e.getMessage(), e);
                  return 0;
                }
              }).forEach(filter::accept);
            }
            catch (IOException e) {
              Logger.getLogger(FiltersTest.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
          }
          catch (IOException e) {
            Logger.getLogger(FiltersTest.class.getName()).log(Level.INFO, e.getMessage(), e);
          }
        }
      });
    }
  }
}