package com.ak.rsm.measurement;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.prediction.TetrapolarPrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;

public record TetrapolarMeasurement(@Nonnull InexactTetrapolarSystem system,
                                    @Nonnegative double resistivity) implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.resistivity(), m.resistivity() * m.system().getApparentRelativeError());

  @Override
  public String toString() {
    return "%s; %s".formatted(system, ValuePair.Name.RHO_1.of(resistivity, resistivity * system.getApparentRelativeError()));
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    var avg = TO_VALUE.apply(this).mergeWith(TO_VALUE.apply(that));
    double relErrorRho = avg.getAbsError() / avg.getValue();
    double dL = Math.min(system.absError(), that.system().absError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    InexactTetrapolarSystem merged = new InexactTetrapolarSystem(dL, new TetrapolarSystem(sPU, lCC));
    return new TetrapolarMeasurement(merged, avg.getValue());
  }

  @Nonnull
  @Override
  @ParametersAreNonnullByDefault
  public Prediction toPrediction(RelativeMediumLayers kw, @Nonnegative double rho1) {
    return TetrapolarPrediction.of(system, kw, rho1, resistivity);
  }

  @Nonnull
  public static TetrapolarResistance.PreBuilder<Measurement> of(@Nonnull InexactTetrapolarSystem inexact) {
    return new Builder(inexact);
  }

  @Nonnull
  public static PreBuilder<Measurement> milli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  @Nonnull
  public static PreBuilder<Measurement> si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  public interface PreBuilder<T> {
    @Nonnull
    TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC);
  }

  abstract static class AbstractBuilder<T> extends TetrapolarResistance.AbstractBuilder<T> implements PreBuilder<T> {
    @Nonnegative
    protected final double absError;
    protected InexactTetrapolarSystem inexact;

    AbstractBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }

    AbstractBuilder(@Nonnull InexactTetrapolarSystem inexact) {
      super(DoubleUnaryOperator.identity());
      absError = inexact.absError();
      this.inexact = inexact;
    }

    @Nonnull
    @Override
    public final TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC) {
      inexact = new InexactTetrapolarSystem(absError, new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC)));
      return this;
    }
  }

  private static class Builder extends AbstractBuilder<Measurement> {
    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    private Builder(@Nonnull InexactTetrapolarSystem inexact) {
      super(inexact);
    }

    @Nonnull
    @Override
    public Measurement rho(@Nonnegative double rho) {
      return new TetrapolarMeasurement(inexact, TetrapolarResistance.of(inexact.system()).rho(rho).resistivity());
    }

    @Nonnull
    @Override
    public Measurement ofOhms(@Nonnegative double rOhms) {
      return new TetrapolarMeasurement(inexact, TetrapolarResistance.of(inexact.system()).ofOhms(rOhms).resistivity());
    }

    @Override
    @Nonnull
    public Measurement build() {
      if (Double.isNaN(hStep)) {
        return new TetrapolarMeasurement(inexact,
            TetrapolarResistance.of(inexact.system()).rho1(rho1).rho2(rho2).h(h).resistivity()
        );
      }
      else {
        return new TetrapolarMeasurement(inexact,
            TetrapolarResistance.of(inexact.system()).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1).resistivity()
        );
      }
    }
  }
}
