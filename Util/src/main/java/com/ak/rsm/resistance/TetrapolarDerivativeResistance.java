package com.ak.rsm.resistance;

import java.util.Arrays;
import java.util.Collection;
import java.util.PrimitiveIterator;
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
  public static PreBuilder ofSI(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(DoubleUnaryOperator.identity(), sPU, lCC);
  }

  @Nonnull
  public static PreBuilder ofMilli(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(Metrics.MILLI, sPU, lCC);
  }

  @Nonnull
  public static MultiPreBuilder milli() {
    return new MultiBuilder(Metrics.MILLI);
  }

  public interface PreBuilder {
    @Nonnull
    TetrapolarResistance.PreBuilder<DerivativeResistance> dh(double dh);
  }

  public interface MultiPreBuilder {
    @Nonnull
    TetrapolarResistance.MultiPreBuilder<DerivativeResistance> dh(double dh);
  }

  public record DhHolder(@Nonnull DoubleUnaryOperator converter, double dh) {
    public DhHolder(@Nonnull DoubleUnaryOperator converter, double dh) {
      this.converter = converter;
      this.dh = converter.applyAsDouble(dh);
    }
  }

  private static class Builder extends TetrapolarResistance.AbstractTetrapolarBuilder<DerivativeResistance>
      implements PreBuilder {
    private DhHolder dhHolder;

    private Builder(@Nonnull TetrapolarSystem system) {
      super(DoubleUnaryOperator.identity(), system);
    }

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sPU, @Nonnegative double lCC) {
      super(converter, sPU, lCC);
    }

    @Nonnull
    @Override
    public TetrapolarResistance.PreBuilder<DerivativeResistance> dh(double dh) {
      dhHolder = new DhHolder(converter, dh);
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
      if (rOhms.length == 2) {
        TetrapolarResistance.PreBuilder<Resistance> b = TetrapolarResistance.of(system);
        return new TetrapolarDerivativeResistance(b.ofOhms(rOhms[0]), b.ofOhms(rOhms[1]), dhHolder.dh);
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(rOhms));
      }
    }

    @Nonnull
    @Override
    public DerivativeResistance build() {
      var builder = TetrapolarResistance.of(system).rho1(rho1).rho2(rho2);
      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeResistance(builder.h(h), builder.h(h + dhHolder.dh), dhHolder.dh);
      }
      else {
        return new TetrapolarDerivativeResistance(
            builder.rho3(rho3).hStep(hStep).p(p1, p2mp1),
            builder.rho3(rho3).hStep(hStep + dhHolder.dh).p(p1, p2mp1), dhHolder.dh);
      }
    }
  }

  private static class MultiBuilder
      extends TetrapolarResistance.AbstractMultiTetrapolarBuilder<DerivativeResistance>
      implements MultiPreBuilder {
    private DhHolder dhHolder;

    protected MultiBuilder(@Nonnull DoubleUnaryOperator converter) {
      super(converter);
    }

    @Nonnull
    @Override
    public TetrapolarResistance.MultiPreBuilder<DerivativeResistance> dh(double dh) {
      dhHolder = new DhHolder(converter, dh);
      return this;
    }

    @Nonnull
    @Override
    public Collection<DerivativeResistance> rho(@Nonnegative double rho) {
      return systems.stream().map(s -> new Builder(s).dh(dhHolder.dh).rho(rho)).toList();
    }

    @Nonnull
    @Override
    public Collection<DerivativeResistance> ofOhms(@Nonnull double... rOhms) {
      PrimitiveIterator.OfDouble ohmsBefore = Arrays.stream(rOhms).limit(rOhms.length / 2).iterator();
      PrimitiveIterator.OfDouble ohmsAfter = Arrays.stream(rOhms).skip(rOhms.length / 2).iterator();
      return systems.stream()
          .map(s -> new Builder(s).dh(dhHolder.dh).ofOhms(ohmsBefore.nextDouble(), ohmsAfter.nextDouble()))
          .toList();
    }

    @Nonnull
    @Override
    public Collection<DerivativeResistance> build() {
      return systems.stream()
          .map(
              s -> {
                Builder builder = new Builder(s);
                builder.h = h;
                builder.dhHolder = dhHolder;
                return builder.rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1);
              })
          .toList();
    }
  }
}
