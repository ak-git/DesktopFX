package com.ak.math;

import com.ak.util.Metrics;
import com.ak.util.Strings;

import java.util.Objects;
import java.util.StringJoiner;

import static com.ak.util.Strings.PLUS_MINUS;
import static com.ak.util.Strings.SPACE;
import static javax.measure.MetricPrefix.MILLI;
import static tech.units.indriya.unit.Units.METRE;

public class ValuePair {
  public enum Name {
    NONE,
    RHO {
      @Override
      String toString(String base) {
        return Strings.rho(base);
      }
    },
    RHO_1 {
      @Override
      String toString(String base) {
        return Strings.rho(1, base);
      }
    },
    RHO_2 {
      @Override
      String toString(String base) {
        return Strings.rho(2, base);
      }
    },
    RHO_3 {
      @Override
      String toString(String base) {
        return Strings.rho(3, base);
      }
    },
    H {
      @Override
      double convert(double si) {
        return Metrics.Length.METRE.to(si, MILLI(METRE));
      }

      @Override
      String toString(String base) {
        return "h = %s %s".formatted(base, MILLI(METRE));
      }
    },
    H1 {
      @Override
      double convert(double si) {
        return H.convert(si);
      }

      @Override
      String toString(String base) {
        return Strings.h(1, base);
      }
    },
    H2 {
      @Override
      double convert(double si) {
        return H.convert(si);
      }

      @Override
      String toString(String base) {
        return Strings.h(2, base);
      }
    },
    DH2 {
      @Override
      double convert(double si) {
        return H.convert(si);
      }

      @Override
      String toString(String base) {
        return "%s%s".formatted(Strings.CAP_DELTA, Strings.h(2, base));
      }
    },
    K12 {
      @Override
      String toString(String base) {
        return "k%s%s = %s".formatted(Strings.low(1), Strings.low(2), base);
      }
    },
    K23 {
      @Override
      String toString(String base) {
        return "k%s%s = %s".formatted(Strings.low(2), Strings.low(3), base);
      }
    },
    H_L {
      @Override
      String toString(String base) {
        return "%s = %s".formatted(Strings.PHI, base);
      }
    },
    ERR {
      @Override
      String toString(String base) {
        return "%s = %s".formatted(Strings.EPSILON, base);
      }
    };

    double convert(double si) {
      return si;
    }

    String toString(String base) {
      return base;
    }

    public final ValuePair of(double value, double absError) {
      return new ValuePair(this, value, absError);
    }
  }

  private final Name name;
  private final double value;
  private final double absError;

  private ValuePair(Name name, double value, double absError) {
    this.name = Objects.requireNonNull(name);
    this.value = value;
    this.absError = Double.isNaN(value) ? Double.NaN : Math.abs(absError);
  }

  public Name name() {
    return name;
  }

  public double value() {
    return value;
  }

  public double absError() {
    return absError;
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
      return Double.isFinite(v) ? name.toString("%6.3f".formatted(v).strip()) : name.toString(Double.toString(v));
    }
  }

  public static String format(double value, int afterZero) {
    return "%%.%df".formatted(afterZero).formatted(value);
  }

  public static int afterZero(double absError) {
    if (absError > 0.0) {
      return (int) Math.abs(Math.min(Math.floor(StrictMath.log10(absError)), 0));
    }
    else {
      return 1;
    }
  }

  public ValuePair mergeWith(ValuePair that) {
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
