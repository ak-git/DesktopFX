package com.ak.rsm.measurement;

import com.ak.math.ValuePair;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static tech.units.indriya.unit.Units.OHM;

public record TetrapolarMeasurement(InexactTetrapolarSystem toInexact,
                                    @Nonnegative double resistivity) implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.resistivity(), m.resistivity() * m.toInexact().getApparentRelativeError());

  @Override
  public String toString() {
    return "%s; R = %.3f %s; %s"
        .formatted(toInexact, ohms(), OHM, ValuePair.Name.RHO.of(resistivity, resistivity * toInexact.getApparentRelativeError()));
  }

  @Override
  public double ohms() {
    return TetrapolarResistance.of(system()).rho(resistivity()).ohms();
  }

  @Override
  public Measurement merge(Measurement that) {
    var avg = TO_VALUE.apply(this).mergeWith(TO_VALUE.apply(that));
    double relErrorRho = avg.absError() / avg.value();
    double dL = Math.min(toInexact.absError(), that.toInexact().absError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    InexactTetrapolarSystem merged = new InexactTetrapolarSystem(dL, new TetrapolarSystem(sPU, lCC));
    return new TetrapolarMeasurement(merged, avg.value());
  }

  public static TetrapolarResistance.PreBuilder<Measurement> of(InexactTetrapolarSystem inexact) {
    return new Builder(inexact);
  }

  public static PreBuilder<Measurement> ofSI(@Nonnegative double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  public static PreBuilder<Measurement> ofMilli(@Nonnegative double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  public static MultiPreBuilder<Measurement> milli(double absError) {
    return new MultiBuilder(Metrics.MILLI, absError);
  }

  public interface PreBuilder<T> {
    TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC);
  }

  public interface MultiPreBuilder<T> {
    MultiPreBuilder<T> withShiftError();

    TetrapolarResistance.PreBuilder<Collection<T>> system2(@Nonnegative double sBase);

    TetrapolarResistance.PreBuilder<Collection<T>> system4(@Nonnegative double sBase);
  }

  abstract static class AbstractBuilder<T> extends TetrapolarResistance.AbstractBuilder<T> {
    protected final double absError;

    AbstractBuilder(DoubleUnaryOperator converter, double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }
  }

  abstract static class AbstractSingleBuilder<T> extends AbstractBuilder<T> implements PreBuilder<T> {
    private @Nullable InexactTetrapolarSystem inexact;

    AbstractSingleBuilder(DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    AbstractSingleBuilder(InexactTetrapolarSystem inexact) {
      super(DoubleUnaryOperator.identity(), inexact.absError());
      this.inexact = inexact;
    }

    protected final InexactTetrapolarSystem inexact() {
      return Objects.requireNonNull(inexact);
    }

    @Override
    public final TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC) {
      if (inexact != null) {
        throw new IllegalStateException("Inexact measurement [%s] was already set".formatted(inexact));
      }
      inexact = new InexactTetrapolarSystem(absError, new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC)));
      return this;
    }
  }

  abstract static class AbstractMultiBuilder<T> extends AbstractBuilder<Collection<T>> implements MultiPreBuilder<T> {
    protected final Collection<InexactTetrapolarSystem> inexact = new LinkedList<>();
    private boolean shiftErrorFlag;

    AbstractMultiBuilder(DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    @Override
    public final MultiPreBuilder<T> withShiftError() {
      shiftErrorFlag = true;
      return this;
    }

    @Override
    public final TetrapolarResistance.PreBuilder<Collection<T>> system2(@Nonnegative double sBase) {
      inexact.addAll(
          TetrapolarResistance.AbstractMultiTetrapolarBuilder.system2(converter, sBase).stream().map(toInexact()).toList()
      );
      return this;
    }

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
    private Builder(DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    private Builder(InexactTetrapolarSystem inexact) {
      super(inexact);
    }

    @Override
    public Measurement rho(double... rhos) {
      return TetrapolarResistance.PreBuilder.check(rhos, rho -> new TetrapolarMeasurement(inexact(), rho));
    }

    @Override
    public Measurement ofOhms(double... rOhms) {
      return new TetrapolarMeasurement(inexact(), TetrapolarResistance.of(inexact().system()).ofOhms(rOhms).resistivity());
    }

    @Override
    public Measurement build() {
      if (Double.isNaN(hStep)) {
        return new TetrapolarMeasurement(inexact(),
            TetrapolarResistance.of(inexact().system()).rho1(rho1).rho2(rho2).h(h).resistivity()
        );
      }
      else {
        return new TetrapolarMeasurement(inexact(),
            TetrapolarResistance.of(inexact().system()).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hStep).p(p1, p2mp1).resistivity()
        );
      }
    }
  }

  private static class MultiBuilder extends AbstractMultiBuilder<Measurement> {
    private MultiBuilder(DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    @Override
    public Collection<Measurement> rho(double... rhos) {
      return inexact.stream().map(s -> new Builder(s).rho(rhos)).toList();
    }

    @Override
    public Collection<Measurement> ofOhms(double... rOhms) {
      if (inexact.size() == rOhms.length) {
        PrimitiveIterator.OfDouble ofDouble = Arrays.stream(rOhms).iterator();
        return inexact.stream().map(s -> new Builder(s).ofOhms(ofDouble.nextDouble())).toList();
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(rOhms));
      }
    }

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
