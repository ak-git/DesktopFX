package com.ak.rsm.resistance;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;

import static tec.uom.se.unit.Units.OHM;

public record TetrapolarResistance(@Nonnull TetrapolarSystem system, @Nonnegative double ohms,
                                   @Nonnegative double resistivity) implements Resistance {
  @Override
  public String toString() {
    return "%s; %.3f %s; %s".formatted(system, ohms, OHM, Strings.rho(resistivity));
  }

  @Nonnull
  public static PreBuilder<Resistance> of(@Nonnull TetrapolarSystem system) {
    return new Builder(system);
  }

  @Nonnull
  public static PreBuilder<Resistance> ofSI(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(DoubleUnaryOperator.identity(), sPU, lCC);
  }

  @Nonnull
  public static PreBuilder<Resistance> ofMilli(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(Metrics.MILLI, sPU, lCC);
  }

  @Nonnull
  public static MultiPreBuilder<Resistance> milli() {
    return new MultiBuilder(Metrics.MILLI);
  }

  public interface PreBuilder<T> extends com.ak.util.Builder<T> {
    @Nonnull
    T rho(@Nonnull double... rhos);

    @Nonnull
    T ofOhms(@Nonnull double... rOhms);

    @Nonnull
    LayersBuilder1<T> rho1(@Nonnegative double rho1);

    @ParametersAreNonnullByDefault
    static <T> T check(double[] values, DoubleFunction<T> function) {
      if (values.length == 1) {
        return function.apply(values[0]);
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(values));
      }
    }
  }

  public interface MultiPreBuilder<T> {
    /**
     * Generates optimal electrode system pair.
     * <p>
     * For 10 mm: <b>10 x 30, 50 x 30 mm</b>
     * </p>
     *
     * @param sBase small sPU base.
     * @return builder to make two measurements.
     */
    @Nonnull
    PreBuilder<Collection<T>> system2(@Nonnegative double sBase);

    /**
     * Generates optimal electrode system pair.
     * <p>
     * For 10 mm: <b>10 x 30, 50 x 30, 20 x 40, 60 x 40 mm</b>
     * </p>
     *
     * @param sBase small sPU base in millimeters.
     * @return builder to make four measurements.
     */
    PreBuilder<Collection<T>> system4(@Nonnegative double sBase);

    @ParametersAreNonnullByDefault
    static <R, S> Collection<R> iterate(Collection<S> systems, double[] values, BiFunction<S, double[], R> function) {
      if (systems.size() == values.length) {
        PrimitiveIterator.OfDouble it = Arrays.stream(values).iterator();
        return systems.stream().map(s -> function.apply(s, new double[] {it.nextDouble()})).toList();
      }
      else {
        throw new IllegalArgumentException("%s %s".formatted(systems, Arrays.toString(values)));
      }
    }
  }

  public interface LayersBuilder1<T> {
    @Nonnull
    LayersBuilder2<T> rho2(@Nonnegative double rho2);
  }

  public interface LayersBuilder2<T> {
    @Nonnull
    T h(@Nonnegative double h);

    @Nonnull
    LayersBuilder3<T> rho3(@Nonnegative double rho3);
  }

  public interface LayersBuilder3<T> {
    @Nonnull
    LayersBuilder4<T> hStep(@Nonnegative double hStep);
  }

  public interface LayersBuilder4<T> {
    @Nonnull
    T p(@Nonnegative int p1, @Nonnegative int p2mp1);
  }

  public abstract static class AbstractBuilder<T>
      implements PreBuilder<T>, LayersBuilder1<T>, LayersBuilder2<T>, LayersBuilder3<T>, LayersBuilder4<T> {
    @Nonnull
    protected final DoubleUnaryOperator converter;
    @Nonnegative
    protected double rho1;
    @Nonnegative
    protected double rho2;
    @Nonnegative
    protected double h;
    @Nonnegative
    protected double rho3;
    @Nonnegative
    protected double hStep = Double.NaN;
    @Nonnegative
    protected int p1;
    @Nonnegative
    protected int p2mp1;

    protected AbstractBuilder(@Nonnull DoubleUnaryOperator converter) {
      this.converter = converter;
    }

    @Override
    @Nonnull
    public final LayersBuilder1<T> rho1(@Nonnegative double rho1) {
      this.rho1 = rho1;
      return this;
    }

    @Override
    @Nonnull
    public final LayersBuilder2<T> rho2(@Nonnegative double rho2) {
      this.rho2 = rho2;
      return this;
    }

    @Override
    @Nonnull
    public final T h(@Nonnegative double h) {
      this.h = converter.applyAsDouble(h);
      return build();
    }

    @Nonnull
    @Override
    public LayersBuilder3<T> rho3(@Nonnegative double rho3) {
      this.rho3 = rho3;
      return this;
    }

    @Nonnull
    @Override
    public LayersBuilder4<T> hStep(@Nonnegative double hStep) {
      this.hStep = converter.applyAsDouble(hStep);
      return this;
    }

    @Nonnull
    @Override
    public T p(@Nonnegative int p1, @Nonnegative int p2mp1) {
      this.p1 = p1;
      this.p2mp1 = p2mp1;
      return build();
    }
  }

  public abstract static class AbstractTetrapolarBuilder<T> extends AbstractBuilder<T> {
    @Nonnull
    protected final TetrapolarSystem system;

    @ParametersAreNonnullByDefault
    protected AbstractTetrapolarBuilder(DoubleUnaryOperator converter, TetrapolarSystem system) {
      super(converter);
      this.system = system;
    }

    protected AbstractTetrapolarBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sPU, @Nonnegative double lCC) {
      this(converter, new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC)));
    }
  }

  public abstract static class AbstractMultiTetrapolarBuilder<T> extends AbstractBuilder<Collection<T>> implements MultiPreBuilder<T> {
    @Nonnull
    protected final Collection<TetrapolarSystem> systems = new LinkedList<>();

    @ParametersAreNonnullByDefault
    protected AbstractMultiTetrapolarBuilder(DoubleUnaryOperator converter) {
      super(converter);
    }

    @Nonnull
    @Override
    public final PreBuilder<Collection<T>> system2(@Nonnegative double sBase) {
      systems.addAll(system2(converter, sBase));
      return this;
    }

    @Nonnull
    @Override
    public final PreBuilder<Collection<T>> system4(@Nonnegative double sBase) {
      systems.addAll(system4(converter, sBase));
      return this;
    }

    public static Collection<TetrapolarSystem> system2(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sBase) {
      return List.of(
          new TetrapolarSystem(converter.applyAsDouble(sBase), converter.applyAsDouble(sBase * 3.0)),
          new TetrapolarSystem(converter.applyAsDouble(sBase * 5.0), converter.applyAsDouble(sBase * 3.0))
      );
    }

    public static Collection<TetrapolarSystem> system4(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sBase) {
      return List.of(
          new TetrapolarSystem(converter.applyAsDouble(sBase), converter.applyAsDouble(sBase * 3.0)),
          new TetrapolarSystem(converter.applyAsDouble(sBase * 5.0), converter.applyAsDouble(sBase * 3.0)),
          new TetrapolarSystem(converter.applyAsDouble(sBase * 2.0), converter.applyAsDouble(sBase * 4.0)),
          new TetrapolarSystem(converter.applyAsDouble(sBase * 6.0), converter.applyAsDouble(sBase * 4.0))
      );
    }
  }

  private static final class Builder extends AbstractTetrapolarBuilder<Resistance> {
    @ParametersAreNonnullByDefault
    private Builder(@Nonnull TetrapolarSystem system) {
      super(DoubleUnaryOperator.identity(), system);
    }

    private Builder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double sPU, @Nonnegative double lCC) {
      super(converter, sPU, lCC);
    }

    @Nonnull
    @Override
    public Resistance rho(@Nonnull double... rhos) {
      return PreBuilder.check(rhos, rho -> new TetrapolarResistance(system, new Resistance1Layer(system).value(rho), rho));
    }

    @Nonnull
    @Override
    public Resistance ofOhms(@Nonnull double... rOhms) {
      return PreBuilder.check(rOhms, r -> new TetrapolarResistance(system, r, r / new Resistance1Layer(system).value(1.0)));
    }

    @Override
    @Nonnull
    public Resistance build() {
      if (Double.isNaN(hStep)) {
        return ofOhms(new Resistance2Layer(system).value(rho1, rho2, h));
      }
      else {
        return ofOhms(new Resistance3Layer(system, hStep).value(rho1, rho2, rho3, p1, p2mp1));
      }
    }
  }

  private static class MultiBuilder extends AbstractMultiTetrapolarBuilder<Resistance> {
    private MultiBuilder(@Nonnull DoubleUnaryOperator converter) {
      super(converter);
    }

    @Nonnull
    @Override
    public Collection<Resistance> rho(@Nonnull double... rhos) {
      return MultiPreBuilder.iterate(systems, rhos, (s, rho) -> new Builder(s).rho(rho));
    }

    @Nonnull
    @Override
    public Collection<Resistance> ofOhms(@Nonnull double... rOhms) {
      return MultiPreBuilder.iterate(systems, rOhms, (s, ohm) -> new Builder(s).ofOhms(ohm));
    }

    @Nonnull
    @Override
    public Collection<Resistance> build() {
      return systems.stream().map(s -> {
        Builder builder = new Builder(s);
        builder.h = h;
        return builder.rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1);
      }).toList();
    }
  }
}