package com.ak.rsm.measurement;

import java.util.Collection;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;

public record TetrapolarDerivativeMeasurement(@Nonnull Measurement measurement,
                                              double derivativeResistivity) implements DerivativeMeasurement {
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
  public Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1) {
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

  /**
   * Generates optimal electrode system pair.
   * <p>
   * For 10 mm: <b>10 x 30, 50 x 30 mm</b>
   * </p>
   *
   * @param absError absolute error in millimeters.
   * @param sBase    small sPU base in millimeters.
   * @return builder to make two measurements.
   */
  @Nonnull
  public static TetrapolarResistance.PreBuilder<Collection<DerivativeMeasurement>> milli2(
      @Nonnegative double absError, @Nonnegative double sBase, double dh) {
    return new MultiBuilder(Metrics.MILLI, absError)
        .dh(dh)
        .system(sBase, sBase * 3.0).system(sBase * 5.0, sBase * 3.0);
  }

  @Nonnull
  public static TetrapolarResistance.PreBuilder<Collection<DerivativeMeasurement>> milli2Err(
      double error, @Nonnegative double sBase, double dh) {
    return new MultiBuilder(Metrics.MILLI, Math.abs(error))
        .dh(dh)
        .system(sBase + error, sBase * 3.0 - error).system(sBase * 5.0 + error, sBase * 3.0 - error);
  }

  public interface PreBuilder {
    @Nonnull
    TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(double dh);
  }

  public interface MultiPreBuilder extends TetrapolarMeasurement.MultiPreBuilder<Collection<DerivativeMeasurement>> {
    @Nonnull
    TetrapolarMeasurement.MultiPreBuilder<Collection<DerivativeMeasurement>> dh(double dh);
  }

  private static class Builder extends TetrapolarMeasurement.AbstractSingleBuilder<DerivativeMeasurement> implements PreBuilder {
    private TetrapolarDerivativeResistance.DhHolder dhHolder;

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    private Builder(@Nonnull InexactTetrapolarSystem inexact) {
      super(inexact);
    }

    @Nonnull
    @Override
    public TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(double dh) {
      dhHolder = new TetrapolarDerivativeResistance.DhHolder(converter, dh);
      return this;
    }

    @Nonnull
    @Override
    public DerivativeMeasurement rho(@Nonnegative double rho) {
      return new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).rho(rho), 0.0);
    }

    @Nonnull
    @Override
    public DerivativeMeasurement ofOhms(@Nonnull double... rOhms) {
      return new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).ofOhms(rOhms), 0.0);
    }

    @Nonnull
    @Override
    public DerivativeMeasurement build() {
      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).rho1(rho1).rho2(rho2).h(h), TetrapolarDerivativeResistance.of(inexact.system()).dh(dhHolder.dh()).rho1(rho1).rho2(rho2).h(h).derivativeResistivity());
      }
      else {
        return new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1), TetrapolarDerivativeResistance.of(inexact.system()).dh(dhHolder.dh()).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1).derivativeResistivity());
      }
    }
  }

  private static class MultiBuilder extends TetrapolarMeasurement.AbstractMultiBuilder<Collection<DerivativeMeasurement>> implements MultiPreBuilder {
    private TetrapolarDerivativeResistance.DhHolder dhHolder;

    private MultiBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public TetrapolarMeasurement.MultiPreBuilder<Collection<DerivativeMeasurement>> dh(double dh) {
      dhHolder = new TetrapolarDerivativeResistance.DhHolder(converter, dh);
      return this;
    }

    @Nonnull
    @Override
    public Collection<DerivativeMeasurement> rho(@Nonnegative double rho) {
      return inexact.stream().map(system -> new Builder(system).dh(dhHolder.dh()).rho(rho)).toList();
    }

    @Nonnull
    @Override
    public Collection<DerivativeMeasurement> ofOhms(@Nonnull double... rOhms) {
      return ofOhms(inexact, (rOhm, system) -> new Builder(system).ofOhms(rOhm), rOhms);
    }

    @Nonnull
    @Override
    public Collection<DerivativeMeasurement> build() {
      if (Double.isNaN(hStep)) {
        return inexact.stream().map(s -> new Builder(s).dh(dhHolder.dh()).rho1(rho1).rho2(rho2).h(h)).toList();
      }
      else {
        return inexact.stream().map(s -> new Builder(s).dh(dhHolder.dh()).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1)).toList();
      }
    }
  }
}
