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
    if (sPU >= lCC) {
      throw new IllegalArgumentException();
    }
    sPotentialUnitSI = toDouble(sPU, unit);
    lCurrentCarryingSI = toDouble(lCC, unit);
  }

  TetrapolarSystem newWithError(double eL, @Nonnull Unit<Length> unit) {
    double eSI = toDouble(eL, unit);
    return new TetrapolarSystem(sPotentialUnitSI + eSI, lCurrentCarryingSI - eSI, METRE);
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param resistance in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  @Nonnegative
  double getApparent(@Nonnegative double resistance) {
    return 0.5 * Math.PI * resistance / (1.0 / radiusMinus() - 1.0 / radiusPlus());
  }

  @Nonnegative
  double getL() {
    return lCurrentCarryingSI;
  }

  double radiusMinus() {
    return lCurrentCarryingSI - sPotentialUnitSI;
  }

  double radiusPlus() {
    return lCurrentCarryingSI + sPotentialUnitSI;
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
    return Objects.equals(sPotentialUnitSI, that.sPotentialUnitSI) && Objects.equals(lCurrentCarryingSI, that.lCurrentCarryingSI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sPotentialUnitSI, lCurrentCarryingSI);
  }

  @Override
  public String toString() {
    return String.format("%s x %s",
        Quantities.getQuantity(sPotentialUnitSI, METRE).to(MILLI(METRE)),
        Quantities.getQuantity(lCurrentCarryingSI, METRE).to(MILLI(METRE)));
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  private static double toDouble(@Nonnegative double sPU, @Nonnull Unit<Length> unit) {
    return Quantities.getQuantity(sPU, unit).to(METRE).getValue().doubleValue();
  }
}

