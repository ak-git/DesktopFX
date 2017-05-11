package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class SmoothingImpulsiveNoiseFilter extends AbstractUnaryFilter {
  @Nonnull
  private final HoldFilter holdFilter;

  SmoothingImpulsiveNoiseFilter(@Nonnegative int size) {
    holdFilter = new HoldFilter(size);
    DigitalFilter decimationFilter = new DecimationFilter(size, operand -> {
      int[] sorted = holdFilter.getSorted();
      double mean = Arrays.stream(sorted).average().orElse(0.0);

      int posCount = 0;
      double distances = 0.0;
      for (int n : sorted) {
        if (n > mean) {
          posCount++;
          distances += (n - mean);
        }
      }
      return (int) Math.round(mean + (posCount - (size - posCount)) * distances / StrictMath.pow(size, 2));
    });
    DigitalFilter interpolationFilter = new LinearInterpolationFilter(size);
    interpolationFilter.forEach(this::publish);
    decimationFilter.forEach(interpolationFilter);
    holdFilter.forEach(decimationFilter);
  }

  @Override
  public double getDelay() {
    return 0;
  }

  @Override
  void publishUnary(int in) {
    holdFilter.accept(in);
  }
}
