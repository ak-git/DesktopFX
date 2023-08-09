package com.ak.rsm.measurement;

import com.ak.math.ValuePair;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PrimitiveIterator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static tec.uom.se.unit.Units.OHM;

public record TetrapolarMeasurement(@Nonnull InexactTetrapolarSystem inexact,
                                    @Nonnegative double resistivity) implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.resistivity(), m.resistivity() * m.inexact().getApparentRelativeError());

  @Override
  public String toString() {
    return "%s; R = %.3f %s; %s"
        .formatted(inexact, ohms(), OHM, ValuePair.Name.RHO.of(resistivity, resistivity * inexact.getApparentRelativeError()));
  }

  @Override
  public double ohms() {
    return TetrapolarResistance.of(system()).rho(resistivity()).ohms();
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    var avg = TO_VALUE.apply(this).mergeWith(TO_VALUE.apply(that));
    double relErrorRho = avg.absError() / avg.value();
    double dL = Math.min(inexact.absError(), that.inexact().absError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    InexactTetrapolarSystem merged = new InexactTetrapolarSystem(dL, new TetrapolarSystem(sPU, lCC));
    return new TetrapolarMeasurement(merged, avg.value());
  }

  @Nonnull
  public static TetrapolarResistance.PreBuilder<Measurement> of(@Nonnull InexactTetrapolarSystem inexact) {
    return new Builder(inexact);
  }

  @Nonnull
  public static PreBuilder<Measurement> ofSI(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  @Nonnull
  public static PreBuilder<Measurement> ofMilli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  @Nonnull
  public static MultiPreBuilder<Measurement> milli(double absError) {
    return new MultiBuilder(Metrics.MILLI, absError);
  }

  public interface PreBuilder<T> {
    @Nonnull
    TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC);
  }

  public interface MultiPreBuilder<T> {
    @Nonnull
    MultiPreBuilder<T> withShiftError();

    @Nonnull
    TetrapolarResistance.PreBuilder<Collection<T>> system2(@Nonnegative double sBase);

    @Nonnull
    TetrapolarResistance.PreBuilder<Collection<T>> system4(@Nonnegative double sBase);
  }

  abstract static class AbstractBuilder<T> extends TetrapolarResistance.AbstractBuilder<T> {
    protected final double absError;

    AbstractBuilder(@Nonnull DoubleUnaryOperator converter, double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }
  }

  abstract static class AbstractSingleBuilder<T> extends AbstractBuilder<T> implements PreBuilder<T> {
    protected InexactTetrapolarSystem inexact;

    AbstractSingleBuilder(@Nonnull DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    AbstractSingleBuilder(@Nonnull InexactTetrapolarSystem inexact) {
      super(DoubleUnaryOperator.identity(), inexact.absError());
      this.inexact = inexact;
    }

    @Nonnull
    @Override
    public final TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC) {
      inexact = new InexactTetrapolarSystem(absError, new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC)));
      return this;
    }
  }

  abstract static class AbstractMultiBuilder<T> extends AbstractBuilder<Collection<T>> implements MultiPreBuilder<T> {
    @Nonnull
    protected final Collection<InexactTetrapolarSystem> inexact = new LinkedList<>();
    private boolean shiftErrorFlag;

    AbstractMultiBuilder(@Nonnull DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public final MultiPreBuilder<T> withShiftError() {
      shiftErrorFlag = true;
      return this;
    }

    @Nonnull
    @Override
    public final TetrapolarResistance.PreBuilder<Collection<T>> system2(@Nonnegative double sBase) {
      inexact.addAll(
          TetrapolarResistance.AbstractMultiTetrapolarBuilder.system2(converter, sBase).stream().map(toInexact()).toList()
      );
      return this;
    }

    @Nonnull
    @Override
    public final TetrapolarResistance.PreBuilder<Collection<T>> system4(@Nonnegative double sBase) {
      inexact.addAll(
          TetrapolarResistance.AbstractMultiTetrapolarBuilder.system4(converter, sBase).stream().map(toInexact()).toList()
      );
      return this;
    }

    private Function<TetrapolarSystem, InexactTetrapolarSystem> toInexact() {
      return system -> {
        TetrapolarSystem s = shiftErrorFlag ? new TetrapolarSystem(system.sPU() + absError, system.lCC() - absError) : system;
        return new InexactTetrapolarSystem(absError, s);
      };
    }
  }

  private static class Builder extends AbstractSingleBuilder<Measurement> {
    private Builder(@Nonnull DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    private Builder(@Nonnull InexactTetrapolarSystem inexact) {
      super(inexact);
    }

    @Nonnull
    @Override
    public Measurement rho(@Nonnull double... rhos) {
      return TetrapolarResistance.PreBuilder.check(rhos, rho -> new TetrapolarMeasurement(inexact, rho));
    }

    @Nonnull
    @Override
    public Measurement ofOhms(@Nonnull double... rOhms) {
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

  private static class MultiBuilder extends AbstractMultiBuilder<Measurement> {
    private MultiBuilder(@Nonnull DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public Collection<Measurement> rho(@Nonnull double... rhos) {
      return inexact.stream().map(s -> new Builder(s).rho(rhos)).toList();
    }

    @Nonnull
    @Override
    public Collection<Measurement> ofOhms(@Nonnull double... rOhms) {
      if (inexact.size() == rOhms.length) {
        PrimitiveIterator.OfDouble ofDouble = Arrays.stream(rOhms).iterator();
        return inexact.stream().map(s -> new Builder(s).ofOhms(ofDouble.nextDouble())).toList();
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(rOhms));
      }
    }

    @Nonnull
    @Override
    public Collection<Measurement> build() {
      if (Double.isNaN(hStep)) {
        return inexact.stream().map(s -> new Builder(s).rho1(rho1).rho2(rho2).h(h)).toList();
      }
      else {
        return inexact.stream().map(s -> new Builder(s).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1)).toList();
      }
    }
  }
}
