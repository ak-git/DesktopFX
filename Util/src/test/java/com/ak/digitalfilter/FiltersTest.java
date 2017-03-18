package com.ak.digitalfilter;

import java.util.IntSummaryStatistics;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

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
        1, (IntBinaryOperator) Filters::cathetus, 0, 1
    }, {
        10, (IntBinaryOperator) Filters::cathetus, 0, 10
    }, {
        100, (IntBinaryOperator) Filters::cathetus, 12, 106
    }, {
        1000, (IntBinaryOperator) Filters::cathetus, 124, 1062
    }, {
        10000, (IntBinaryOperator) Filters::cathetus, 1250, 10625
    }};
  }

  @Test(dataProvider = "cathetus")
  public static void testCathetus(@Nonnegative int c, @Nonnull IntBinaryOperator cathetusOperator, @Nonnegative int min, @Nonnegative int max) {
    IntSummaryStatistics stat = IntStream.range(0, c + 1).map(b -> cathetusOperator.applyAsInt(b, c)).summaryStatistics();
    Assert.assertEquals(stat.getMin(), min, Integer.toString(stat.getMin()));
    Assert.assertEquals(stat.getMax(), max, Integer.toString(stat.getMax()));
  }
}