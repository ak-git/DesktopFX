package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import com.ak.util.Strings;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnegative;
import javax.measure.MetricPrefix;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import static tech.units.indriya.unit.Units.METRE;

public record TetrapolarDerivativeResistance(Resistance resistance, double derivativeResistivity, double dh)
    implements DerivativeResistance {
  private TetrapolarDerivativeResistance(Resistance resistance, Resistivity resistanceAfter, double dh) {
    this(resistance, (resistanceAfter.resistivity() - resistance.resistivity()) / (dh / resistance.system().lCC()), dh);
  }

  @Override
  public String toString() {
    String s = "%s; %s".formatted(resistance, Strings.dRhoByPhi(derivativeResistivity));
    if (Double.isNaN(dh)) {
      return s;
    }
    else {
      return "%s; dh = %.3f %s".formatted(s, Metrics.Length.METRE.to(dh, MetricPrefix.MILLI(METRE)), MetricPrefix.MILLI(METRE));
    }
  }

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

  public static PreBuilder of(TetrapolarSystem system) {
    return new Builder(system);
  }

  public static PreBuilder ofSI(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(DoubleUnaryOperator.identity(), sPU, lCC);
  }

  public static PreBuilder ofMilli(@Nonnegative double sPU, @Nonnegative double lCC) {
    return new Builder(Metrics.MILLI, sPU, lCC);
  }

  public static MultiPreBuilder milli() {
    return new MultiBuilder(Metrics.MILLI);
  }

  public interface PreBuilder {
    TetrapolarResistance.PreBuilder<DerivativeResistance> dh(double dh);

    static <T> T check(double[] values, Supplier<T> supplier) {
      if (values.length == 2) {
        return supplier.get();
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(values));
      }
    }
  }

  public interface MultiPreBuilder {
    TetrapolarResistance.MultiPreBuilder<DerivativeResistance> dh(double dh);

    static <R, S> Collection<R> split(Collection<S> systems, double[] values, BiFunction<S, double[], R> function) {
      if (systems.size() * 2 == values.length) {
        PrimitiveIterator.OfDouble before = Arrays.stream(values).limit(values.length / 2).iterator();
        PrimitiveIterator.OfDouble after = Arrays.stream(values).skip(values.length / 2).iterator();
        return systems.stream()
            .map(s -> function.apply(s, new double[] {before.nextDouble(), after.nextDouble()}))
            .toList();
      }
      else {
        throw new IllegalArgumentException("%s %s".formatted(systems, Arrays.toString(values)));
      }
    }
  }

  public record DhHolder(DoubleUnaryOperator converter, double dh) {
    public DhHolder(DoubleUnaryOperator converter, double dh) {
      this.converter = converter;
      this.dh = converter.applyAsDouble(dh);
    }
  }

  private static class Builder extends TetrapolarResistance.AbstractTetrapolarBuilder<DerivativeResistance>
      implements PreBuilder {
    private @Nullable DhHolder dhHolder;

    private Builder(TetrapolarSystem system) {
      super(DoubleUnaryOperator.identity(), system);
    }

    private Builder(DoubleUnaryOperator converter, @Nonnegative double sPU, @Nonnegative double lCC) {
      super(converter, sPU, lCC);
    }

    @Override
    public TetrapolarResistance.PreBuilder<DerivativeResistance> dh(double dh) {
      dhHolder = new DhHolder(converter, dh);
      return this;
    }

    @Override
    public DerivativeResistance rho(double... rhos) {
      if (Double.isNaN(Objects.requireNonNull(dhHolder).dh)) {
        return PreBuilder.check(rhos,
            () -> new TetrapolarDerivativeResistance(TetrapolarResistance.of(system).rho(rhos[0]), rhos[1], Double.NaN)
        );
      }
      else {
        throw new IllegalStateException(Double.toString(dhHolder.dh));
      }
    }

    @Override
    public DerivativeResistance ofOhms(double... rOhms) {
      return PreBuilder.check(rOhms,
          () -> {
            TetrapolarResistance.PreBuilder<Resistance> b = TetrapolarResistance.of(system);
            return new TetrapolarDerivativeResistance(b.ofOhms(rOhms[0]), b.ofOhms(rOhms[1]), Objects.requireNonNull(dhHolder).dh);
          }
      );
    }

    @Override
    public DerivativeResistance build() {
      var builder = TetrapolarResistance.of(system).rho1(rho1).rho2(rho2);
      double dh = Objects.requireNonNull(dhHolder).dh;
      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeResistance(builder.h(h), builder.h(h + dh), dh);
      }
      else {
        if (Double.isNaN(dh)) {
          throw new IllegalArgumentException("dh NULL is not supported in 3-layer model");
        }
        if (Math.abs(dh) < hStep) {
          throw new IllegalArgumentException("|dh| < hStep -> |%s| < %s".formatted(dh, hStep));
        }

        var builder3 = builder.rho3(rho3).hStep(hStep);
        return new TetrapolarDerivativeResistance(
            builder3.p(p1, p2mp1),
            builder3.p(p1 + Numbers.toInt(dh / hStep), p2mp1),
            dh);
      }
    }
  }

  private static class MultiBuilder
      extends TetrapolarResistance.AbstractMultiTetrapolarBuilder<DerivativeResistance>
      implements MultiPreBuilder {
    private @Nullable DhHolder dhHolder;

    protected MultiBuilder(DoubleUnaryOperator converter) {
      super(converter);
    }

    @Override
    public TetrapolarResistance.MultiPreBuilder<DerivativeResistance> dh(double dh) {
      dhHolder = new DhHolder(converter, dh);
      return this;
    }

    @Override
    public Collection<DerivativeResistance> rho(double... rhos) {
      return MultiPreBuilder.split(systems, rhos, (s, rho) -> new Builder(s).dh(Objects.requireNonNull(dhHolder).dh).rho(rho));
    }

    @Override
    public Collection<DerivativeResistance> ofOhms(double... rOhms) {
      return MultiPreBuilder.split(systems, rOhms, (s, ohms) -> new Builder(s).dh(Objects.requireNonNull(dhHolder).dh).ofOhms(ohms));
    }

    @Override
    public Collection<DerivativeResistance> build() {
      return systems.stream()
          .map(
              s -> {
                Builder builder = new Builder(s);
                builder.h = h;
                builder.dhHolder = Objects.requireNonNull(dhHolder);
                return builder.rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1);
              })
          .toList();
    }
  }
}
