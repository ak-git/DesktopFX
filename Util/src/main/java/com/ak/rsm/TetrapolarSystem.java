package com.ak.rsm;

import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.ak.util.Metrics;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;

public final class TetrapolarSystem {
  @Nonnegative
  private final double sPotentialUnitSI;
  @Nonnegative
  private final double lCurrentCarryingSI;

  public TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC, @Nonnull Unit<Length> unit) {
    sPotentialUnitSI = toDouble(Math.min(sPU, lCC), unit);
    lCurrentCarryingSI = toDouble(Math.max(sPU, lCC), unit);
  }

  double radius(double sign) {
    return Math.abs(lCurrentCarryingSI + Math.signum(sign) * sPotentialUnitSI) / 2.0;
  }

  @Nonnegative
  double getL() {
    return lCurrentCarryingSI;
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param rOhms in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  public double getApparent(@Nonnegative double rOhms) {
    return rOhms * Math.PI / (Math.abs(1.0 / radius(-1.0)) - Math.abs(1.0 / radius(1.0)));
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
    return Objects.equals(Math.min(sPotentialUnitSI, lCurrentCarryingSI), Math.min(that.sPotentialUnitSI, that.lCurrentCarryingSI)) &&
        Objects.equals(Math.max(sPotentialUnitSI, lCurrentCarryingSI), Math.max(that.sPotentialUnitSI, that.lCurrentCarryingSI));
  }

  @Override
  public int hashCode() {
    return Objects.hash(Math.min(sPotentialUnitSI, lCurrentCarryingSI), Math.max(sPotentialUnitSI, lCurrentCarryingSI));
  }

  @Override
  public String toString() {
    return "%2.0f x %2.0f %s".formatted(Metrics.toMilli(sPotentialUnitSI), Metrics.toMilli(lCurrentCarryingSI), MetricPrefix.MILLI(METRE));
  }

  @Nonnull
  TetrapolarSystem newWithError(@Nonnegative double absErrorSI, int signS, int signL) {
    return new TetrapolarSystem(
        sPotentialUnitSI + Math.signum(signS) * absErrorSI,
        lCurrentCarryingSI + Math.signum(signL) * absErrorSI, METRE);
  }

  @Nonnegative
  private static double toDouble(@Nonnegative double sPU, @Nonnull Unit<Length> unit) {
    return Math.abs(Quantities.getQuantity(sPU, unit).to(METRE).getValue().doubleValue());
  }
}

