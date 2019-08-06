package com.ak.rsm;

import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import tec.uom.se.quantity.Quantities;

import static tec.uom.se.unit.MetricPrefix.MILLI;
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
    return (lCurrentCarryingSI - sPotentialUnitSI) / 2.0;
  }

  double radiusPls() {
    return (lCurrentCarryingSI + sPotentialUnitSI) / 2.0;
  }

  double sToL() {
    return sPotentialUnitSI / lCurrentCarryingSI;
  }

  double Lh(double h) {
    return lCurrentCarryingSI / h;
  }

  double h(double Lh) {
    return lCurrentCarryingSI / Lh;
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
    return String.format("%s x %s",
        Quantities.getQuantity(sPotentialUnitSI, METRE).to(MILLI(METRE)),
        Quantities.getQuantity(lCurrentCarryingSI, METRE).to(MILLI(METRE)));
  }

  private static double toDouble(@Nonnegative double sPU, @Nonnull Unit<Length> unit) {
    return Quantities.getQuantity(sPU, unit).to(METRE).getValue().doubleValue();
  }
}

