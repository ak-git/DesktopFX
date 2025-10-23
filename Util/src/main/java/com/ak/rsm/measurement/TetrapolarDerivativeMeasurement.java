package com.ak.rsm.measurement;

import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.DerivativeResistance;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;

import javax.measure.MetricPrefix;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.OHM;

public record TetrapolarDerivativeMeasurement(Measurement measurement, double derivativeResistivity, double dh)
    implements DerivativeMeasurement {
  public String toString() {
    String s = "%s; %s".formatted(measurement, Strings.dRhoByPhi(derivativeResistivity));
    if (Double.isNaN(dh)) {
      return s;
    }
    else {
      return "%s; %s = %.3f %s; dh = %.3f mm".formatted(s, Strings.CAP_DELTA, dOhms(), OHM, Metrics.Length.METRE.to(dh, MetricPrefix.MILLI(METRE)));
    }
  }

  @Override
  public InexactTetrapolarSystem toInexact() {
    return measurement.toInexact();
  }

  @Override
  public double resistivity() {
    return measurement.resistivity();
  }

  @Override
  public double ohms() {
    return measurement.ohms();
  }

  @Override
  public double dOhms() {
    return TetrapolarResistance.of(system()).rho(derivativeResistivity * dh / measurement.system().lCC()).ohms();
  }

  @Override
  public Measurement merge(Measurement that) {
    return measurement.merge(Objects.requireNonNull(that));
  }

  public static PreBuilder ofSI(double absError) {
    return new Builder(DoubleUnaryOperator.identity(), absError);
  }

  public static PreBuilder ofMilli(double absError) {
    return new Builder(Metrics.MILLI, absError);
  }

  public static MultiPreBuilder milli(double absError) {
    return new MultiBuilder(Metrics.MILLI, absError);
  }

  public interface PreBuilder {
    TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(DeltaH dh);
  }

  public interface MultiPreBuilder {
    TetrapolarMeasurement.MultiPreBuilder<DerivativeMeasurement> dh(DeltaH dh);
  }

  private static class Builder extends TetrapolarMeasurement.AbstractSingleBuilder<DerivativeMeasurement> implements PreBuilder {
    private DeltaH dh = DeltaH.NULL;

    private Builder(DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    private Builder(InexactTetrapolarSystem inexact) {
      super(inexact);
    }

    @Override
    public TetrapolarMeasurement.PreBuilder<DerivativeMeasurement> dh(DeltaH dh) {
      this.dh = dh.convert(converter);
      return this;
    }

    @Override
    public DerivativeMeasurement rho(double... rhos) {
      if (dh.type() == DeltaH.Type.NONE) {
        return TetrapolarDerivativeResistance.PreBuilder.check(rhos,
            () -> new TetrapolarDerivativeMeasurement(TetrapolarMeasurement.of(inexact()).rho(rhos[0]), rhos[1], Double.NaN)
        );
      }
      else {
        throw new IllegalStateException(
            "dh = %s is not needed when rho and dRho = %s are exist".formatted(dh, Arrays.toString(rhos))
        );
      }
    }

    @Override
    public DerivativeMeasurement ofOhms(double... rOhms) {
      if (rOhms.length == 2) {
        DerivativeResistance derivativeResistance = TetrapolarDerivativeResistance.of(inexact().system()).dh(dh).ofOhms(rOhms);
        return new TetrapolarDerivativeMeasurement(
            TetrapolarMeasurement.of(inexact()).ofOhms(rOhms[0]),
            derivativeResistance.derivativeResistivity(),
            derivativeResistance.dh()
        );
      }
      else {
        throw new IllegalArgumentException(Arrays.toString(rOhms));
      }
    }

    @Override
    public DerivativeMeasurement build() {
      TetrapolarResistance.LayersBuilder2<Measurement> b = TetrapolarMeasurement.of(inexact()).rho1(rho1).rho2(rho2);
      TetrapolarResistance.LayersBuilder2<DerivativeResistance> d = TetrapolarDerivativeResistance.of(inexact().system())
          .dh(dh).rho1(rho1).rho2(rho2);

      if (Double.isNaN(hStep)) {
        return new TetrapolarDerivativeMeasurement(b.h(h), d.h(h).derivativeResistivity(), dh.value());
      }
      else {
        DerivativeResistance derivativeResistance = d.rho3(rho3).hStep(hStep).p(p1, p2mp1);
        return new TetrapolarDerivativeMeasurement(
            b.rho3(rho3).hStep(hStep).p(p1, p2mp1),
            derivativeResistance.derivativeResistivity(),
            derivativeResistance.dh()
        );
      }
    }
  }

  private static class MultiBuilder extends TetrapolarMeasurement.AbstractMultiBuilder<DerivativeMeasurement> implements MultiPreBuilder {
    private DeltaH dh = DeltaH.NULL;

    private MultiBuilder(DoubleUnaryOperator converter, double absError) {
      super(converter, absError);
    }

    @Override
    public TetrapolarMeasurement.MultiPreBuilder<DerivativeMeasurement> dh(DeltaH dh) {
      this.dh = dh.convert(converter);
      return this;
    }

    @Override
    public Collection<DerivativeMeasurement> rho(double... rhos) {
      return TetrapolarDerivativeResistance.MultiPreBuilder.split(inexact, rhos,
          (s, rho) -> newBuilder(s).rho(rho)
      );
    }

    @Override
    public Collection<DerivativeMeasurement> ofOhms(double... rOhms) {
      return TetrapolarDerivativeResistance.MultiPreBuilder.split(inexact, rOhms,
          (s, ohms) -> newBuilder(s).ofOhms(ohms)
      );
    }

    @Override
    public Collection<DerivativeMeasurement> build() {
      Function<InexactTetrapolarSystem, TetrapolarResistance.LayersBuilder2<DerivativeMeasurement>> builder2Function =
          s -> newBuilder(s).rho1(rho1).rho2(rho2);

      if (Double.isNaN(hStep)) {
        return inexact.stream().map(s -> builder2Function.apply(s).h(h)).toList();
      }
      else {
        return inexact.stream().map(s -> builder2Function.apply(s).rho3(rho3).hStep(hStep).p(p1, p2mp1)).toList();
      }
    }

    private Builder newBuilder(InexactTetrapolarSystem s) {
      Builder builder = new Builder(s);
      builder.dh = dh;
      return builder;
    }
  }
}
