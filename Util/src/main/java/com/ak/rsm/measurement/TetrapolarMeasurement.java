package com.ak.rsm.measurement;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.math.ValuePair;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;

public record TetrapolarMeasurement(@Nonnull InexactTetrapolarSystem system,
                                    @Nonnegative double resistivity) implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.resistivity(), m.resistivity() * m.system().getApparentRelativeError());

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

  @Override
  public String toString() {
    return "%s; %s".formatted(system, ValuePair.Name.RHO_1.of(resistivity, resistivity * system.getApparentRelativeError()));
  }

  @Nonnull
  public static PreBuilder milli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  @Nonnull
  public static PreBuilder si(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  public interface PreBuilder extends TetrapolarResistance.PreBuilder<Measurement> {
    @Nonnull
    TetrapolarResistance.PreBuilder<Measurement> system(@Nonnegative double sPU, @Nonnegative double lCC);
  }

  private static class Builder extends TetrapolarResistance.AbstractBuilder<Measurement> implements PreBuilder {
    @Nonnegative
    private final double absError;
    private InexactTetrapolarSystem inexact;

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }

    @Nonnull
    @Override
    public TetrapolarResistance.PreBuilder<Measurement> system(@Nonnegative double sPU, @Nonnegative double lCC) {
      inexact = new InexactTetrapolarSystem(absError, new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC)));
      return this;
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
        return new TetrapolarMeasurement(inexact, TetrapolarResistance.of(inexact.system()).rho1(rho1).rho2(rho2).h(h).resistivity());
      }
      else {
        return new TetrapolarMeasurement(inexact,
            TetrapolarResistance.of(inexact.system()).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1).resistivity());
      }
    }
  }
}
