package com.ak.rsm2;

import com.ak.rsm.system.Layers;
import com.ak.util.Builder;
import com.ak.util.Metrics;

import javax.measure.MetricPrefix;
import java.util.Objects;

import static java.lang.StrictMath.pow;
import static tech.units.indriya.unit.Units.METRE;

public sealed interface ElectrodeSystem {
  double sToL();

  static ElectrodeSystem of(double sToL) {
    return new RelativeRecord(sToL);
  }

  static Step1 ofMilli() {
    return new Tetrapolar.TetrapolarBuilder(Metrics.Length.MILLI);
  }

  record RelativeRecord(double sToL) implements ElectrodeSystem {
    public RelativeRecord {
      if (Double.isFinite(sToL)) {
        sToL = Math.abs(sToL);
      }
      else {
        throw new IllegalArgumentException("s / L is %s".formatted(sToL));
      }

      if (Double.compare(sToL, 0.0) == 0) {
        throw new IllegalArgumentException("s / L cannot be zero");
      }
      else if (Double.compare(sToL, 1.0) == 0) {
        throw new IllegalArgumentException("s cannot be equals to L");
      }
    }

    @Override
    public String toString() {
      return "s / L = %.3f".formatted(sToL);
    }
  }

  sealed interface Step1 {
    Step2 tetrapolar(double sPU, double lCC);
  }

  sealed interface Tetrapolar extends ElectrodeSystem {
    double sPU();

    double lCC();

    default double phi(double hSI) {
      if (hSI < 0) {
        throw new IllegalArgumentException("h = %f must be non-negative".formatted(hSI));
      }
      else {
        return hSI * (1.0 / Math.abs(lCC() - sPU()) - 1.0 / Math.abs(lCC() + sPU()));
      }
    }

    record TetrapolarRecord(RelativeRecord relative, double sPU, double lCC) implements Tetrapolar {
      public TetrapolarRecord {
        Objects.requireNonNull(relative);
        sPU = Math.abs(sPU);
        lCC = Math.abs(lCC);
      }

      public TetrapolarRecord(double sPU, double lCC) {
        this(new RelativeRecord(sPU / lCC), sPU, lCC);
      }

      @Override
      public double sToL() {
        return relative.sToL();
      }

      @Override
      public String toString() {
        return "%4.1f x %4.1f %s".formatted(
            Metrics.Length.METRE.to(sPU, MetricPrefix.MILLI(METRE)),
            Metrics.Length.METRE.to(lCC, MetricPrefix.MILLI(METRE)),
            MetricPrefix.MILLI(METRE)
        );
      }
    }

    final class TetrapolarBuilder implements Step1 {
      private final Metrics.Length units;

      public TetrapolarBuilder(Metrics.Length units) {
        this.units = units;
      }

      @Override
      public Step2 tetrapolar(double sPU, double lCC) {
        return new Inexact.InexactBuilder(units, new TetrapolarRecord(units.to(sPU, METRE), units.to(lCC, METRE)));
      }
    }
  }

  sealed interface Step2 extends Builder<Tetrapolar> {
    Builder<Inexact> absError(double absError);
  }

  sealed interface Inexact extends Tetrapolar {
    double apparentRhoRelativeError();

    double hMax(K k);

    double hMin(K k);

    record InexactRecord(Tetrapolar tetrapolar, double absError) implements Inexact {
      public InexactRecord {
        Objects.requireNonNull(tetrapolar);
        absError = Math.abs(absError);
        if (Double.compare(absError, 0.0) == 0) {
          throw new IllegalArgumentException("absError cannot be zero");
        }
      }

      @Override
      public String toString() {
        String s = tetrapolar().toString();
        double metre = hMax(K.PLUS_ONE);
        return "%s / %.1f %s; ↕ %.0f %s".formatted(
            s, Metrics.Length.METRE.to(absError, MetricPrefix.MILLI(METRE)), MetricPrefix.MILLI(METRE),
            Metrics.Length.METRE.to(metre, MetricPrefix.MILLI(METRE)), MetricPrefix.MILLI(METRE));
      }

      @Override
      public double sToL() {
        return tetrapolar.sToL();
      }

      @Override
      public double sPU() {
        return tetrapolar.sPU();
      }

      @Override
      public double lCC() {
        return tetrapolar.lCC();
      }

      /**
       * dRho / Rho = E * dL / L
       *
       * @return relative apparent error
       */
      @Override
      public double apparentRhoRelativeError() {
        double x = normalizedSToL();
        return Math.abs((1.0 + x) / (x * (1.0 - x)) * relativeError());
      }

      @Override
      public double hMax(K k) {
        double zeta3 = Math.abs(Layers.sum(n -> pow(k.value(), n) / pow(n, 3.0)));
        double x = normalizedSToL();
        double result = x * pow(1.0 - x, 2.0) * zeta3 / (32.0 * relativeError());
        return pow(result, 1.0 / 3.0) * maxDim();
      }

      @Override
      public double hMin(K k) {
        if (k.isPlusOne()) {
          return 0.0;
        }
        else {
          double result = 4.0;
          if (!k.isMinusOne()) {
            result = (1.0 + k.value()) / (1.0 - k.value()) / Math.abs(Layers.sum(n -> pow(k.value(), n) * pow(n, 2.0)));
          }
          double x = normalizedSToL();
          result *= (1.0 - x) * pow(1.0 + x, 3.0) / (x * (pow(x, 2.0) + 3.0));
          return maxDim() * Math.sqrt(result * relativeError()) / 4.0;
        }
      }

      private double relativeError() {
        return absError / maxDim();
      }

      private double normalizedSToL() {
        return Math.min(sToL(), 1.0 / sToL());
      }

      private double maxDim() {
        return Math.max(sPU(), lCC());
      }
    }

    final class InexactBuilder implements Step2 {
      private final Metrics.Length units;
      private final Tetrapolar electrodeSystem;

      public InexactBuilder(Metrics.Length units, Tetrapolar electrodeSystem) {
        this.units = units;
        this.electrodeSystem = electrodeSystem;
      }

      @Override
      public Tetrapolar build() {
        return electrodeSystem;
      }

      @Override
      public Builder<Inexact> absError(double absError) {
        return () -> new InexactRecord(electrodeSystem, units.toSI(absError));
      }
    }
  }
}
