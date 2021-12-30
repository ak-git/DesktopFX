package com.ak.rsm.measurement;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;

public record TetrapolarDerivativeMeasurement(@Nonnull Measurement measurement, double derivativeResistivity)
    implements DerivativeMeasurement {
  public String toString() {
    return "%s; %s".formatted(measurement, Strings.dRhoByPhi(derivativeResistivity));
  }

  @Nonnull
  @Override
  public InexactTetrapolarSystem inexact() {
    return measurement.inexact();
  }

  @Override
  public double resistivity() {
    return measurement.resistivity();
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    throw new UnsupportedOperationException(that.toString());
  }

  @Nonnull
  @Override
  public Prediction toPrediction(RelativeMediumLayers kw, double rho1) {
    return TetrapolarDerivativePrediction.of(inexact(), kw, rho1, new double[] {resistivity(), derivativeResistivity()});
  }

  @Nonnull
  public static PreBuilder milli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  @Nonnull
  public static PreBuilder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  public interface PreBuilder {
    @Nonnull
    TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(double dh);
  }

  private static class Builder extends TetrapolarMeasurement.AbstractBuilder<DerivativeMeasurement>
      implements PreBuilder {
    private double dh;

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(double dh) {
      this.dh = converter.applyAsDouble(dh);
      return this;
    }

    @Nonnull
    @Override
    public DerivativeMeasurement rho(@Nonnegative double rho) {
      return new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).rho(rho), 0.0);
    }

    @Nonnull
    @Override
    public DerivativeMeasurement ofOhms(@Nonnegative double rOhms) {
      return new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).ofOhms(rOhms), 0.0);
    }

    @Nonnull
    @Override
    public DerivativeMeasurement build() {
      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeMeasurement(
            TetrapolarMeasurement.of(inexact).rho1(rho1).rho2(rho2).h(h),
            TetrapolarDerivativeResistance.of(inexact.system()).dh(dh).rho1(rho1).rho2(rho2).h(h).derivativeResistivity()
        );
      }
      else {
        return new TetrapolarDerivativeMeasurement(
            TetrapolarMeasurement.of(inexact).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1),
            TetrapolarDerivativeResistance
                .of(inexact.system()).dh(dh).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1).derivativeResistivity()
        );
      }
    }
  }
}
