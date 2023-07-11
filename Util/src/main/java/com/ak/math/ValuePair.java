package com.ak.math;

import com.ak.util.Metrics;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.StringJoiner;

import static com.ak.util.Strings.PLUS_MINUS;
import static com.ak.util.Strings.SPACE;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public record ValuePair(@Nonnull Name name, double value, @Nonnegative double absError) {
  public enum Name {
    NONE,
    RHO {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return Strings.rho(base);
      }
    },
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
    RHO_3 {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return Strings.rho(3, base);
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
    K23 {
      @Nonnull
      @Override
      String toString(@Nonnull String base) {
        return "k%s%s = %s".formatted(Strings.low(2), Strings.low(3), base);
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
      return new ValuePair(this, value, Double.isNaN(value) ? Double.NaN : absError);
    }
  }

  public ValuePair(@Nonnull Name name, double value, @Nonnegative double absError) {
    this.name = Objects.requireNonNull(name);
    this.value = value;
    this.absError = Math.abs(absError);
  }

  @Override
  public String toString() {
    double v = name.convert(value);
    double e = name.convert(absError);
    if (absError > 0) {
      int afterZero = afterZero(e);
      return name.toString(
          new StringJoiner(SPACE)
              .add(format(v, afterZero)).add(PLUS_MINUS).add(format(e, afterZero + 1)).toString()
      );
    }
    else {
      return name.toString("%f".formatted(v));
    }
  }

  @Nonnull
  public static String format(double value, @Nonnegative int afterZero) {
    return "%%.%df".formatted(afterZero).formatted(value);
  }

  public static int afterZero(@Nonnegative double absError) {
    if (absError > 0.0) {
      return (int) Math.abs(Math.min(Math.floor(StrictMath.log10(absError)), 0));
    }
    else {
      return 1;
    }
  }

  @Nonnull
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
