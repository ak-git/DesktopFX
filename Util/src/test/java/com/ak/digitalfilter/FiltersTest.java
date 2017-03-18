package com.ak.digitalfilter;

import java.util.DoubleSummaryStatistics;
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
        1, (IntBinaryOperator) Filters::hypot02, 0.30
    }, {
        10, (IntBinaryOperator) Filters::hypot02, 0.05
    }, {
        100, (IntBinaryOperator) Filters::hypot02, 0.025
    }, {
        1000, (IntBinaryOperator) Filters::hypot02, 0.021
    }, {
        10000, (IntBinaryOperator) Filters::hypot02, 0.02
    }, {
        1, (IntBinaryOperator) Filters::hypot63, 0.30
    }, {
        10, (IntBinaryOperator) Filters::hypot63, 0.12
    }, {
        100, (IntBinaryOperator) Filters::hypot63, 0.063
    }};
  }

  @Test(dataProvider = "hypot")
  public static void testHypot(@Nonnegative int a, @Nonnull IntBinaryOperator hOperator, @Nonnegative double error) {
    DoubleSummaryStatistics stat = IntStream.range(0, a + 1).
        mapToDouble(b -> {
          double hypot = StrictMath.hypot(a, b);
          return Math.max(
              Math.abs(1.0 - hOperator.applyAsInt(a, b) / hypot),
              Math.abs(1.0 - hOperator.applyAsInt(b, a) / hypot)
          );
        }).summaryStatistics();
    Assert.assertFalse(stat.getMin() < 0, Double.toString(stat.getMin()));
    Assert.assertTrue(stat.getMax() < error, Double.toString(stat.getMax()));
  }
}