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

final class TetrapolarSystem {
  @Nonnegative
  private final double sPotentialUnitSI;
  @Nonnegative
  private final double lCurrentCarryingSI;

  TetrapolarSystem(@Nonnegative double sPU, @Nonnegative double lCC, @Nonnull Unit<Length> unit) {
    sPotentialUnitSI = toDouble(sPU, unit);
    lCurrentCarryingSI = toDouble(lCC, unit);
  }

  double radiusMns() {
    return Math.abs(lCurrentCarryingSI - sPotentialUnitSI) / 2.0;
  }

  double radiusPls() {
    return (lCurrentCarryingSI + sPotentialUnitSI) / 2.0;
  }

  @Nonnegative
  double lToH(double h) {
    return Math.abs(lCurrentCarryingSI / h);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TetrapolarSystem that)) {
      return false;
    }

    return Objects.equals(Math.min(sPotentialUnitSI, lCurrentCarryingSI), Math.min(that.sPotentialUnitSI, that.lCurrentCarryingSI)) &&
        Objects.equals(Math.max(sPotentialUnitSI, lCurrentCarryingSI), Math.max(that.sPotentialUnitSI, that.lCurrentCarryingSI));
  }

  @Override
  public int hashCode() {
    return Objects.hash(Math.min(sPotentialUnitSI, lCurrentCarryingSI), Math.max(sPotentialUnitSI, lCurrentCarryingSI));
  }

  @Override
  public String toString() {
    return String.format("%.0f x %.0f %s", Metrics.toMilli(sPotentialUnitSI), Metrics.toMilli(lCurrentCarryingSI), MetricPrefix.MILLI(METRE));
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

