package com.ak.rsm.resistance;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;

public record TetrapolarDerivativeResistance(@Nonnull Resistance resistance, double derivativeResistivity)
    implements DerivativeResistance {
  private TetrapolarDerivativeResistance(@Nonnull Resistance resistance, @Nonnull Resistance resistanceAfter, double dh) {
    this(resistance, (resistanceAfter.resistivity() - resistance.resistivity()) / (dh / resistance.system().lCC()));
  }

  @Override
  public String toString() {
    return "%s; %s".formatted(resistance, Strings.dRhoByPhi(derivativeResistivity));
  }

  @Nonnull
  @Override
  public TetrapolarSystem system() {
    return resistance.system();
  }

  @Override
  public double ohms() {
    return resistance.ohms();
  }

  @Override
  public double resistivity() {
    return resistance.resistivity();
  }

  @Nonnull
  public static PreBuilder of(@Nonnull TetrapolarSystem system) {
    return new Builder(system);
  }

  @Nonnull
  public static PreBuilder milli(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(Metrics.MILLI, sPU, lCC);
  }

  @Nonnull
  public static PreBuilder si(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(DoubleUnaryOperator.identity(), sPU, lCC);
  }

  public interface PreBuilder {
    @Nonnull
    TetrapolarResistance.PreBuilder<DerivativeResistance> dh(double dh);
  }

  private static class Builder extends TetrapolarResistance.AbstractTetrapolarBuilder<DerivativeResistance> implements PreBuilder {
    private double dh;

    private Builder(@Nonnull TetrapolarSystem system) {
      super(DoubleUnaryOperator.identity(), system);
    }

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sPU, @Nonnegative double lCC) {
      super(converter, sPU, lCC);
    }

    @Nonnull
    @Override
    public TetrapolarResistance.PreBuilder<DerivativeResistance> dh(double dh) {
      this.dh = converter.applyAsDouble(dh);
      return this;
    }

    @Nonnull
    @Override
    public DerivativeResistance rho(@Nonnegative double rho) {
      return new TetrapolarDerivativeResistance(TetrapolarResistance.of(system).rho(rho), 0.0);
    }

    @Nonnull
    @Override
    public DerivativeResistance ofOhms(@Nonnull double... rOhms) {
      return new TetrapolarDerivativeResistance(TetrapolarResistance.of(system).ofOhms(rOhms), 0.0);
    }

    @Nonnull
    @Override
    public DerivativeResistance build() {
      var builder = TetrapolarResistance.of(system).rho1(rho1).rho2(rho2);
      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeResistance(builder.h(h), builder.h(h + dh), dh);
      }
      else {
        return new TetrapolarDerivativeResistance(
            builder.rho3(rho3).hStep(hStep).p(p1, p2mp1),
            builder.rho3(rho3).hStep(hStep + dh).p(p1, p2mp1), dh);
      }
    }
  }
}
