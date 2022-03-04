package com.ak.math;

import java.util.StringJoiner;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import com.ak.util.Strings;

import static com.ak.util.Strings.PLUS_MINUS;
import static com.ak.util.Strings.SPACE;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public final class ValuePair {
  public enum Name {
    NONE,
    RHO_1 {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return Strings.rho(1, base);
      }
    },
    RHO_2 {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return Strings.rho(2, base);
      }
    },
    H {
      @Override
      double convert(double si) {
        return Metrics.toMilli(si);
      }

      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return "h = %s %s".formatted(base, MILLI(METRE));
      }
    },
    K12 {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return "k%s%s = %s".formatted(Strings.low(1), Strings.low(2), base);
      }
    },
    H_L {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return "%s = %s".formatted(Strings.PHI, base);
      }
    };

    double convert(double si) {
      return si;
    }

    @Nonnull
    String toString(@Nonnull String base) {
      return base;
    }

    public final ValuePair of(double value, @Nonnegative double absError) {
      return new ValuePair(this, value, absError);
    }
  }

  @Nonnull
  private final Name name;
  private final double value;
  @Nonnegative
  private final double absError;

  private ValuePair(@Nonnull Name name, double value, @Nonnegative double absError) {
    this.name = name;
    this.value = value;
    this.absError = Math.abs(absError);
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
    double v = name.convert(value);
    double e = name.convert(absError);
    if (absError > 0) {
      int afterZero = (int) Math.abs(Math.min(Math.floor(StrictMath.log10(e)), 0));
      return name.toString(
          new StringJoiner(SPACE)
              .add("%%.%df".formatted(afterZero).formatted(v))
              .add(PLUS_MINUS).add("%%.%df".formatted(afterZero + 1).formatted(e))
              .toString()
      );
    }
    else {
      return name.toString("%f".formatted(v));
    }
  }

  public ValuePair mergeWith(@Nonnull ValuePair that) {
    var sigma1Q = StrictMath.pow(absError, 2.0);
    var sigma2Q = StrictMath.pow(that.absError, 2.0);
    double k = 0.5;
    if (sigma1Q > 0 && sigma2Q > 0) {
      k = sigma2Q / (sigma1Q + sigma2Q);
    }
    double avg = k * value + (1.0 - k) * that.value;
    double sigmaAvg = 1.0 / Math.sqrt((1.0 / sigma1Q + 1.0 / sigma2Q));
    return new ValuePair(name, avg, sigmaAvg);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    return toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
