package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public enum Filters {
  ;

  static Quantity<Time> getDelay(@Nonnull DigitalFilter filter, @Nonnull Quantity<Frequency> frequency) {
    return Quantities.getQuantity(frequency.to(Units.HERTZ).inverse().multiply(filter.getDelay()).getValue().doubleValue(), Units.SECOND);
  }

  @Nonnegative
  static int hypot63(@Nonnegative int a, @Nonnegative int b) {
    int max = Math.max(a, b);
    int min = Math.min(a, b);

    int x = max + (min >> 1);
    return x - (x >> 4);
  }

  @Nonnegative
  static int hypot02(@Nonnegative int a, @Nonnegative int b) {
    int max = Math.max(a, b);
    int min = Math.min(a, b);

    if (min < ((max >> 1) - (max >> 3))) {
      return max + (min >> 3);
    }
    else {
      return max - (max >> 3) - (max >> 5) + (min >> 1) + (min >> 4);
    }
  }

  @Nonnegative
  static int cathetus63(@Nonnegative int hypot, @Nonnegative int cathetus) {
    int c = Math.max(hypot, cathetus);
    int b = Math.min(hypot, cathetus);
    c += c >> 4;
    int a = c - (b >> 1);
    if (a > b) {
      return a;
    }
    else {
      return (c - b) << 1;
    }
  }

  public static int[] sharpingDecimate(@Nonnull int[] ints, @Nonnegative int factor) {
    if (factor < 2) {
      return ints;
    }
    else {
      int[] decimated = new int[ints.length / factor];
      decimated[0] = ints[0];
      for (int i = 1; i < decimated.length; i++) {
        Arrays.sort(ints, i * factor, (i + 1) * factor);
        int min = ints[i * factor];
        int max = ints[(i + 1) * factor - 1];
        if (Math.abs(decimated[i - 1] - max) > Math.abs(decimated[i - 1] - min)) {
          decimated[i] = max;
        }
        else {
          decimated[i] = min;
        }
      }
      return decimated;
    }
  }
}
