package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;

import static java.lang.StrictMath.hypot;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class ResistanceTwoLayer implements TrivariateFunction {
  @Nonnull
  private final ResistanceOneLayer resistanceOneLayer;

  ResistanceTwoLayer(@Nonnull TetrapolarSystem electrodeSystem) {
    resistanceOneLayer = new ResistanceOneLayer(electrodeSystem);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1SI specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2SI specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param hSI    height of <b>1-layer</b> in metres
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(@Nonnegative double rho1SI, @Nonnegative double rho2SI, @Nonnegative double hSI) {
    double resistivity = resistanceOneLayer.value(rho1SI);

    if (Double.compare(rho1SI, rho2SI) == 0) {
      return resistivity;
    }
    else {
      return resistivity + 2.0 * ResistanceOneLayer.twoRhoByPI(rho1SI) * sum(getK12(rho1SI, rho2SI), hSI);
    }
  }

  static double getRho1ToRho2(double k12) {
    return (1.0 - k12) / (1.0 + k12);
  }

  static double getK12(@Nonnegative double rho1SI, @Nonnegative double rho2SI) {
    if (Double.isInfinite(rho2SI)) {
      return 1.0;
    }
    else {
      return (rho2SI - rho1SI) / (rho2SI + rho1SI);
    }
  }

  static double sum(@Nonnegative double hSI, @Nonnull BivariateFunction nAndB) {
    return sum(n -> nAndB.value(n, 4.0 * n * hSI));
  }

  double sum(double k12, @Nonnegative double hSI) {
    return sum(hSI, (n, b) -> pow(k12, n) *
        (1.0 / hypot(resistanceOneLayer.getElectrodeSystem().radiusMinus(), b)
            - 1.0 / hypot(resistanceOneLayer.getElectrodeSystem().radiusPlus(), b)));
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  private static double sum(@Nonnull DoubleUnaryOperator operator) {
    return IntStream.rangeClosed(1, 1024).parallel().mapToDouble(operator::applyAsDouble).sum();
  }
}
