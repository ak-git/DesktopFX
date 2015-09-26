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
  private final Quantity<Length> sPotentialUnitSI;
  private final Quantity<Length> lCurrentCarringSI;

  public TetrapolarSystem(double sPU, double lCC, Unit<Length> unit) {
    if (sPU >= lCC) {
      throw new IllegalArgumentException();
    }
    sPotentialUnitSI = Quantities.getQuantity(sPU, unit).to(METRE);
    lCurrentCarringSI = Quantities.getQuantity(lCC, unit).to(METRE);
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param resistance in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  public double getApparent(Quantity<ElectricResistance> resistance) {
    double sSI = sPotentialUnitSI.getValue().doubleValue();
    double lSI = lCurrentCarringSI.getValue().doubleValue();
    return 0.5 * Math.PI * resistance.to(OHM).getValue().doubleValue() / (1.0 / (lSI - sSI) - 1.0 / (lSI + sSI));
  }

  public Quantity<Length> getS() {
    return sPotentialUnitSI;
  }

  public Quantity<Length> getL() {
    return lCurrentCarringSI;
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
    return Objects.equals(sPotentialUnitSI, that.sPotentialUnitSI) && Objects.equals(lCurrentCarringSI, that.lCurrentCarringSI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sPotentialUnitSI, lCurrentCarringSI);
  }

  @Override
  public String toString() {
    return String.format("%s x %s", sPotentialUnitSI.to(MILLI(METRE)), lCurrentCarringSI.to(MILLI(METRE)));
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}

