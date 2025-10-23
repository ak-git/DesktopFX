package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import com.ak.util.Strings;

import javax.measure.MetricPrefix;
import java.util.Arrays;
import java.util.Collection;
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

  public static PreBuilder ofSI(double sPU, double lCC) {
    return new Builder(DoubleUnaryOperator.identity(), sPU, lCC);
  }

  public static PreBuilder ofMilli(double sPU, double lCC) {
    return new Builder(Metrics.MILLI, sPU, lCC);
  }

  public static MultiPreBuilder milli() {
    return new MultiBuilder(Metrics.MILLI);
  }

  public interface PreBuilder {
    TetrapolarResistance.PreBuilder<DerivativeResistance> dh(DeltaH dh);

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
    TetrapolarResistance.MultiPreBuilder<DerivativeResistance> dh(DeltaH dh);

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

  private static class Builder extends TetrapolarResistance.AbstractTetrapolarBuilder<DerivativeResistance>
      implements PreBuilder {
    private DeltaH dh = DeltaH.NULL;

    private Builder(TetrapolarSystem system) {
      super(DoubleUnaryOperator.identity(), system);
    }

    private Builder(DoubleUnaryOperator converter, double sPU, double lCC) {
      super(converter, sPU, lCC);
    }

    @Override
    public TetrapolarResistance.PreBuilder<DerivativeResistance> dh(DeltaH dh) {
      this.dh = dh.convert(converter);
      return this;
    }

    @Override
    public DerivativeResistance rho(double... rhos) {
      if (dh.type() == DeltaH.Type.NONE) {
        return PreBuilder.check(rhos,
            () -> new TetrapolarDerivativeResistance(TetrapolarResistance.of(system).rho(rhos[0]), rhos[1], Double.NaN)
        );
      }
      else {
        throw new IllegalStateException(dh.toString());
      }
    }

    @Override
    public DerivativeResistance ofOhms(double... rOhms) {
      return PreBuilder.check(rOhms,
          () -> {
            if (dh.next().type() != DeltaH.Type.NONE) {
              throw new IllegalArgumentException("During measure, you do not know about both h1/h2 changes. Only h1 or h2 values allowed");
            }
            TetrapolarResistance.PreBuilder<Resistance> b = TetrapolarResistance.of(system);
            return new TetrapolarDerivativeResistance(b.ofOhms(rOhms[0]), b.ofOhms(rOhms[1]), dh.value());
          }
      );
    }

    @Override
    public DerivativeResistance build() {
      var builder = TetrapolarResistance.of(system).rho1(rho1).rho2(rho2);
      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeResistance(builder.h(h), builder.h(h + dh.value()), dh.value());
      }
      else {
        if (dh.type() == DeltaH.Type.NONE) {
          throw new IllegalArgumentException("dh NULL is not supported in 3-layer model");
        }
        var builder3 = builder.rho3(rho3).hStep(hStep);

        int[] p = Arrays.stream(dh.values()).mapToInt(h -> Numbers.toInt(h / hStep)).toArray();
        Resistance base = builder3.p(p1, p2mp1);
        Resistance step1 = builder3.p(p1 + p[0], p2mp1);
        Resistance step2 = builder3.p(p1, p2mp1 + p[1]);
        Resistance stepTotal = builder3.p(p1 + p[0], p2mp1 + p[1]);

        double dhValue = Math.ceil((step1.resistivity() * p[0] + step2.resistivity() * p[1]) / stepTotal.resistivity()) * hStep;

        if (Math.abs(dhValue) < Math.abs(hStep)) {
          throw new IllegalArgumentException("|dh = %f| < |hStep = %f|".formatted(dhValue, hStep));
        }
        return new TetrapolarDerivativeResistance(base, builder3.p(p1 + p[0], p2mp1 + p[1]), dhValue);
      }
    }
  }

  private static class MultiBuilder
      extends TetrapolarResistance.AbstractMultiTetrapolarBuilder<DerivativeResistance>
      implements MultiPreBuilder {
    private DeltaH dh = DeltaH.NULL;

    protected MultiBuilder(DoubleUnaryOperator converter) {
      super(converter);
    }

    @Override
    public TetrapolarResistance.MultiPreBuilder<DerivativeResistance> dh(DeltaH dh) {
      this.dh = dh.convert(converter);
      return this;
    }

    @Override
    public Collection<DerivativeResistance> rho(double... rhos) {
      return MultiPreBuilder.split(systems, rhos, (s, rho) -> new Builder(s).dh(dh).rho(rho));
    }

    @Override
    public Collection<DerivativeResistance> ofOhms(double... rOhms) {
      return MultiPreBuilder.split(systems, rOhms, (s, ohms) -> new Builder(s).dh(dh).ofOhms(ohms));
    }

    @Override
    public Collection<DerivativeResistance> build() {
      return systems.stream()
          .map(
              s -> {
                Builder builder = new Builder(s);
                builder.h = h;
                builder.dh = dh;
                return builder.rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1);
              })
          .toList();
    }
  }
}
