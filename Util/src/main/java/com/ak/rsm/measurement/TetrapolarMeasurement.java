package com.ak.rsm.measurement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
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

public record TetrapolarMeasurement(@Nonnull InexactTetrapolarSystem inexact,
                                    @Nonnegative double resistivity) implements Measurement {
  private static final Function<Measurement, ValuePair> TO_VALUE =
      m -> ValuePair.Name.RHO_1.of(m.resistivity(), m.resistivity() * m.inexact().getApparentRelativeError());

  @Override
  public String toString() {
    return "%s; %s".formatted(inexact, ValuePair.Name.RHO_1.of(resistivity, resistivity * inexact.getApparentRelativeError()));
  }

  @Nonnull
  @Override
  public Measurement merge(@Nonnull Measurement that) {
    var avg = TO_VALUE.apply(this).mergeWith(TO_VALUE.apply(that));
    double relErrorRho = avg.getAbsError() / avg.getValue();
    double dL = Math.min(inexact.absError(), that.inexact().absError());
    double lCC = RelativeTetrapolarSystem.MIN_ERROR_FACTOR * dL / relErrorRho;
    double sPU = RelativeTetrapolarSystem.OPTIMAL_SL * lCC;
    InexactTetrapolarSystem merged = new InexactTetrapolarSystem(dL, new TetrapolarSystem(sPU, lCC));
    return new TetrapolarMeasurement(merged, avg.getValue());
  }

  @Nonnull
  @Override
  @ParametersAreNonnullByDefault
  public Prediction toPrediction(RelativeMediumLayers kw, @Nonnegative double rho1) {
    return TetrapolarPrediction.of(inexact, kw, rho1, resistivity);
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

  @Nonnull
  public static TetrapolarResistance.PreBuilder<Collection<Measurement>> milli2(@Nonnegative double absError, @Nonnegative double sBase) {
    return new MultiBuilder(Metrics.MILLI, absError)
        .system(sBase, sBase * 3.0).system(sBase * 5.0, sBase * 3.0);
  }

  public interface PreBuilder<T> {
    @Nonnull
    TetrapolarResistance.PreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC);
  }

  public interface MultiPreBuilder<T> extends TetrapolarResistance.PreBuilder<T> {
    @Nonnull
    MultiPreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC);
  }

  abstract static class AbstractBuilder<T> extends TetrapolarResistance.AbstractBuilder<T> {
    @Nonnegative
    protected final double absError;

    AbstractBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter);
      this.absError = converter.applyAsDouble(absError);
    }
  }

  abstract static class AbstractSingleBuilder<T> extends AbstractBuilder<T> implements PreBuilder<T> {
    protected InexactTetrapolarSystem inexact;

    AbstractSingleBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
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

  abstract static class AbstractMultiBuilder<T> extends AbstractBuilder<T> implements MultiPreBuilder<T> {
    @Nonnull
    protected final Deque<InexactTetrapolarSystem> inexact = new LinkedList<>();

    AbstractMultiBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public final MultiPreBuilder<T> system(@Nonnegative double sPU, @Nonnegative double lCC) {
      inexact.add(
          new InexactTetrapolarSystem(absError,
              new TetrapolarSystem(converter.applyAsDouble(sPU), converter.applyAsDouble(lCC))
          )
      );
      return this;
    }
  }

  private static class Builder extends AbstractSingleBuilder<Measurement> {
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

  private static class MultiBuilder extends AbstractMultiBuilder<Collection<Measurement>> {
    private MultiBuilder(@Nonnull DoubleUnaryOperator converter, @Nonnegative double absError) {
      super(converter, absError);
    }

    @Nonnull
    @Override
    public Collection<Measurement> rho(@Nonnegative double rho) {
      return inexact.stream().map(s -> new Builder(s).rho(rho)).toList();
    }

    @Nonnull
    @Override
    public Collection<Measurement> ofOhms(@Nonnull double... rOhms) {
      if (inexact.size() == rOhms.length) {
        Iterator<InexactTetrapolarSystem> iterator = inexact.iterator();
        return Arrays.stream(rOhms).mapToObj(rOhm -> new Builder(iterator.next()).ofOhms(rOhm)).toList();
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
