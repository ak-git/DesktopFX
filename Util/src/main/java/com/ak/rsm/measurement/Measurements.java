package com.ak.rsm.measurement;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer1RelativeMedium;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntConsumer;

import static java.lang.StrictMath.pow;

public enum Measurements {
  ;

  @Nonnegative
  public static double getBaseL(@Nonnull Collection<? extends Resistivity> measurements) {
    return measurements.stream().map(Resistivity::system).mapToDouble(TetrapolarSystem::lCC).max().orElseThrow();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (Layer1RelativeMedium.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.resistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.inexact().getApparentRelativeError());
    }
    else if (Layer1RelativeMedium.NAN.equals(kw)) {
      return ValuePair.Name.RHO_1.of(Double.NaN, Double.NaN);
    }
    else {
      double baseL = getBaseL(measurements);
      return measurements.stream()
          .map(measurement -> {
            TetrapolarSystem s = measurement.system();
            RelativeMediumLayers layer2RelativeMedium = new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / s.lCC());
            double normApparent = Apparent2Rho.newApparentDivRho1(s.relativeSystem()).applyAsDouble(layer2RelativeMedium);

            double fK = Math.abs(Apparent2Rho.newDerApparentByKDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerApparentByPhiDivRho1(s.relativeSystem()).applyAsDouble(kw) * kw.hToLAbsError());

            return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent,
                (fK + fPhi) * measurement.resistivity() / pow(normApparent, 2.0)
            );
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }

  /**
   * Convert real measurements to system-4 virtual results.
   *
   * <h3>TetrapolarMeasurement, 4-values</h1>
   * <p>Real Ohms:</p>
   * <pre>
   *   122.3, 199.0, 66.0, 202.0
   * </pre>
   * <p>Converted to:</p>
   * <pre>
   *    122.3, 199.0, <b>66.0 * 2</b>, <b>202.0 * 2 - 66.0 * 2</b>
   * </pre>
   * <h3>TetrapolarDerivativeMeasurement, 8-values</h1>
   * <pre>
   *   122.3, 199.0, 66.0, 202.0,
   *   122.3 + 0.1, 199.0 + 0.4, 66.0 + 0.1, 202.0 + 0.25
   * </pre>
   * <p>Converted to:</p>
   * <pre>
   *    122.3, 199.0, <b>66.0 * 2</b>, <b>202.0 * 2 - 66.0 * 2</b>,
   *    122.3 + 0.1, 199.0 + 0.4, <b>(66.0 + 0.1) * 2</b>, <b>(202.0 + 0.25) * 2 - (66.0 + 0.1) * 2)</b>
   * </pre>
   *
   * @param s4Direct real Ohms
   * @return system-4 virtual results
   */
  @Nonnull
  public static double[] fixOhms(@Nonnull double... s4Direct) {
    if (s4Direct.length != 4 && s4Direct.length != 8) {
      throw new IllegalArgumentException("Needs 4 or 8 values, but found: " + Arrays.toString(s4Direct));
    }

    double[] fixed = s4Direct.clone();

    IntConsumer fix = i -> {
      fixed[i] = s4Direct[i] * 2;
      fixed[i + 1] = s4Direct[i + 1] * 2 - fixed[i];
    };

    fix.accept(2);
    if (s4Direct.length == 8) {
      fix.accept(2 + s4Direct.length / 2);
    }
    return fixed;
  }
}

