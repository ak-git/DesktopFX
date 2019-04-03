package com.ak.rsm;

import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;

import static java.lang.StrictMath.pow;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class ResistanceTwoLayer implements TrivariateFunction {
  static final int SUM_LIMIT = 1024 * 8;
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

  double sum(double k12, @Nonnegative double hSI) {
    return sum(hSI, n -> pow(k12, n), resistanceOneLayer.qAndB());
  }

  static double sum(@Nonnegative double hSI, @Nonnull IntToDoubleFunction q, @Nonnull BivariateFunction qAndB) {
    return sum(n -> qAndB.value(q.applyAsDouble(n), 4.0 * n * hSI));
  }

  static double sum(@Nonnull IntToDoubleFunction operator) {
    return IntStream.rangeClosed(1, SUM_LIMIT).unordered().parallel().mapToDouble(operator).sum();
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
