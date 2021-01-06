package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public final class TetrapolarSystem extends RelativeTetrapolarSystem {
  @Nonnegative
  private final double sPU;
  @Nonnegative
  private final double lCC;

  private TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC) {
    super(sPU / lCC);
    this.sPU = Math.abs(sPU);
    this.lCC = Math.abs(lCC);
  }

  public double getL() {
    return lCC;
  }

  @Nonnegative
  double getMaxL() {
    return Math.max(sPU, lCC);
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param rOhms in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  @Nonnegative
  public double getApparent(@Nonnegative double rOhms) {
    return (rOhms * Math.PI * lCC / 2) / (1.0 / factor(-1.0) - 1.0 / factor(1.0));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TetrapolarSystem)) {
      return false;
    }

    TetrapolarSystem that = (TetrapolarSystem) o;
    return super.equals(that) && Double.compare(getMaxL(), that.getMaxL()) == 0;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new int[] {super.hashCode(), Double.hashCode(getMaxL())});
  }

  @Override
  public String toString() {
    return "%2.0f x %2.0f %s".formatted(Metrics.toMilli(sPU), Metrics.toMilli(lCC), MetricPrefix.MILLI(METRE));
  }

  @Nonnull
  TetrapolarSystem newWithError(@Nonnegative double absErrorSI, int signS, int signL) {
    return new TetrapolarSystem(
        sPU + Math.signum(signS) * absErrorSI,
        lCC + Math.signum(signL) * absErrorSI
    );
  }

  public static MilliBuilder milli() {
    return new MilliBuilder();
  }

  public static class MilliBuilder {
    @Nonnegative
    private double s;

    public MilliBuilder s(@Nonnegative double smm) {
      s = Metrics.fromMilli(smm);
      return this;
    }

    public TetrapolarSystem l(@Nonnegative double lmm) {
      return new TetrapolarSystem(s, Metrics.fromMilli(lmm));
    }
  }
}

