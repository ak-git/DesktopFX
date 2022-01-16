package com.ak.rsm.measurement;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistance;
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
  public InexactTetrapolarSystem system() {
    return measurement.system();
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
    return TetrapolarDerivativePrediction.of(system(), kw, rho1, new double[] {resistivity(), derivativeResistivity()});
  }

  @Nonnull
  public static PreBuilder ofSI(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  @Nonnull
  public static PreBuilder ofMilli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  @Nonnull
  public static MultiPreBuilder milli(double absError) {
    return new MultiBuilder(Metrics.MILLI, absError);
  }

  public interface PreBuilder {
    @Nonnull
    TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(double dh);
  }

  public interface MultiPreBuilder {
    @Nonnull
    TetrapolarMeasurement.MultiPreBuilder<DerivativeMeasurement> dh(double dh);
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
    public DerivativeMeasurement rho(@Nonnull double... rhos) {
      if (Double.isNaN(dhHolder.dh())) {
        return TetrapolarDerivativeResistance.PreBuilder.check(rhos,
            () -> new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact).rho(rhos[0]), rhos[1])
        );
      }
      else {
        throw new IllegalStateException(Double.toString(dhHolder.dh()));
      }
    }

    @Nonnull
    @Override
    public DerivativeMeasurement ofOhms(@Nonnull double... rOhms) {
      if (rOhms.length == 2) {
        return new TetrapolarDerivativeMeasurement(
            TetrapolarMeasurement.of(inexact).ofOhms(rOhms[0]),
            TetrapolarDerivativeResistance.of(inexact.system()).dh(dhHolder.dh()).ofOhms(rOhms).derivativeResistivity()
        );
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(rOhms));
      }
    }

    @Nonnull
    @Override
    public DerivativeMeasurement build() {
      TetrapolarResistance.LayersBuilder2<Measurement> b = TetrapolarMeasurement.of(inexact).rho1(rho1).rho2(rho2);
      TetrapolarResistance.LayersBuilder2<DerivativeResistance> d = TetrapolarDerivativeResistance.of(inexact.system())
          .dh(dhHolder.dh()).rho1(rho1).rho2(rho2);

      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeMeasurement(b.h(h), d.h(h).derivativeResistivity());
      }
      else {
        return new TetrapolarDerivativeMeasurement(
            b.rho3(rho3).hStep(hStep).p(p1, p2mp1), d.rho3(rho3).hStep(hStep).p(p1, p2mp1).derivativeResistivity()
        );
      }
    }
  }

  private static class MultiBuilder extends TetrapolarMeasurement.AbstractMultiBuilder<DerivativeMeasurement> implements MultiPreBuilder {
    private TetrapolarDerivativeResistance.DhHolder dhHolder;

    private MultiBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public TetrapolarMeasurement.MultiPreBuilder<DerivativeMeasurement> dh(double dh) {
      dhHolder = new TetrapolarDerivativeResistance.DhHolder(converter, dh);
      return this;
    }

    @Nonnull
    @Override
    public Collection<DerivativeMeasurement> rho(@Nonnull double... rhos) {
      return TetrapolarDerivativeResistance.MultiPreBuilder.split(inexact, rhos,
          (s, rho) -> newBuilder(s).rho(rho)
      );
    }

    @Nonnull
    @Override
    public Collection<DerivativeMeasurement> ofOhms(@Nonnull double... rOhms) {
      return TetrapolarDerivativeResistance.MultiPreBuilder.split(inexact, rOhms,
          (s, ohms) -> newBuilder(s).ofOhms(ohms)
      );
    }

    @Nonnull
    @Override
    public Collection<DerivativeMeasurement> build() {
      Function<InexactTetrapolarSystem, TetrapolarResistance.LayersBuilder2<DerivativeMeasurement>> builder2Function =
          s -> newBuilder(s).rho1(rho1).rho2(rho2);

      if (Double.isNaN(hStep)) {
        return inexact.stream().map(s -> builder2Function.apply(s).h(h)).toList();
      }
      else {
        return inexact.stream().map(s -> builder2Function.apply(s).rho3(rho3).hStep(hStep).p(p1, p2mp1)).toList();
      }
    }

    @Nonnull
    private Builder newBuilder(@Nonnull InexactTetrapolarSystem s) {
      Builder builder = new Builder(s);
      builder.dhHolder = dhHolder;
      return builder;
    }
  }
}
