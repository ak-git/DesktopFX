package com.ak.rsm.resistance;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;

import static tec.uom.se.unit.Units.OHM;

public record TetrapolarResistance(@Nonnull TetrapolarSystem system,
                                   @Nonnegative double ohms, @Nonnegative double resistivity) implements Resistance {
  @Override
  public String toString() {
    return "%s; %.3f %s; %s".formatted(system, ohms, OHM, Strings.rho(resistivity));
  }

  @Nonnull
  public static PreBuilder<Resistance> of(@Nonnull TetrapolarSystem system) {
    return new Builder(DoubleUnaryOperator.identity(), system);
  }

  @Nonnull
  public static PreBuilder<Resistance> milli(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(Metrics.MILLI, sPU, lCC);
  }

  @Nonnull
  public static PreBuilder<Resistance> si(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(DoubleUnaryOperator.identity(), sPU, lCC);
  }

  public interface PreBuilder<T> {
    @Nonnull
    T rho(@Nonnegative double rho);

    @Nonnull
    T ofOhms(@Nonnegative double rOhms);

    @Nonnull
    LayersBuilder1<T> rho1(@Nonnegative double rho1);
  }

  public interface LayersBuilder1<T> {
    @Nonnull
    LayersBuilder2<T> rho2(@Nonnegative double rho2);
  }

  public interface LayersBuilder2<T> {
    @Nonnull
    T h(@Nonnegative double h);
  }

  public abstract static class AbstractBuilder<T> implements PreBuilder<T>, LayersBuilder1<T>, LayersBuilder2<T>, com.ak.util.Builder<T> {
    @Nonnull
    protected final DoubleUnaryOperator converter;
    @Nonnegative
    protected double rho1;
    @Nonnegative
    protected double rho2;
    @Nonnegative
    protected double h;

    protected AbstractBuilder(@Nonnull DoubleUnaryOperator converter) {
      this.converter = converter;
    }

    @Override
    @Nonnull
    public LayersBuilder1<T> rho1(@Nonnegative double rho1) {
      this.rho1 = rho1;
      return this;
    }

    @Override
    @Nonnull
    public LayersBuilder2<T> rho2(@Nonnegative double rho2) {
      this.rho2 = rho2;
      return this;
    }

    @Override
    @Nonnull
    public T h(@Nonnegative double h) {
      this.h = converter.applyAsDouble(h);
      return build();
    }
  }

  private static class Builder extends AbstractBuilder<Resistance> {
    @Nonnull
    private final TetrapolarSystem system;

    @ParametersAreNonnullByDefault
    private Builder(DoubleUnaryOperator converter, TetrapolarSystem system) {
      super(converter);
      this.system = system;
    }

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sPU, @Nonnegative double lCC) {
      this(converter, new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC)));
    }

    @Nonnull
    @Override
    public Resistance rho(@Nonnegative double rho) {
      return new TetrapolarResistance(system, new Resistance1Layer(system).value(rho), rho);
    }

    @Nonnull
    @Override
    public Resistance ofOhms(@Nonnegative double rOhms) {
      return new TetrapolarResistance(system, rOhms, rOhms / new Resistance1Layer(system).value(1.0));
    }

    @Override
    @Nonnull
    public Resistance build() {
      return ofOhms(new Resistance2Layer(system).value(rho1, rho2, h));
    }
  }
}
