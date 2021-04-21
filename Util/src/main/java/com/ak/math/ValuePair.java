package com.ak.math;

import java.util.StringJoiner;

import javax.annotation.Nonnegative;

import static com.ak.util.Strings.PLUS_MINUS;
import static com.ak.util.Strings.SPACE;

public final class ValuePair {
  private final double value;
  @Nonnegative
  private final double absError;

  public ValuePair(double value, @Nonnegative double absError) {
    this.value = value;
    this.absError = absError;
  }

  public ValuePair(double value) {
    this(value, 0.0);
  }

  public double getValue() {
    return value;
  }

  @Nonnegative
  public double getAbsError() {
    return absError;
  }

  @Override
  public String toString() {
    if (absError > 0) {
      int afterZero = (int) Math.abs(Math.min(Math.floor(StrictMath.log10(absError)), 0));
      return new StringJoiner(SPACE)
          .add("%%.%df".formatted(afterZero).formatted(value))
          .add(PLUS_MINUS).add("%%.%df".formatted(afterZero + 1).formatted(absError))
          .toString();
    }
    else {
      return Double.toString(value);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    var valuePair = (ValuePair) o;
    return toString().equals(valuePair.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
