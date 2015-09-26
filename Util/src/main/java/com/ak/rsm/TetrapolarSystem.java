package com.ak.rsm;

import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.ElectricResistance;
import javax.measure.quantity.Length;

import tec.uom.se.quantity.Quantities;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public final class TetrapolarSystem implements Cloneable {
  private final double sPotentialUnitSI;
  private final double lCurrentCarryingSI;

  public TetrapolarSystem(double sPU, double lCC, Unit<Length> unit) {
    if (sPU >= lCC) {
      throw new IllegalArgumentException();
    }
    sPotentialUnitSI = Quantities.getQuantity(sPU, unit).to(METRE).getValue().doubleValue();
    lCurrentCarryingSI = Quantities.getQuantity(lCC, unit).to(METRE).getValue().doubleValue();
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param resistance in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  public double getApparent(Quantity<ElectricResistance> resistance) {
    return 0.5 * Math.PI * resistance.to(OHM).getValue().doubleValue() / (1.0 / radiusMinus() - 1.0 / radiusPlus());
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
}

