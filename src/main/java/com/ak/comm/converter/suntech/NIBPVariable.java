package com.ak.comm.converter.suntech;

import javax.measure.Unit;
import javax.measure.quantity.Pressure;

import com.ak.comm.converter.Variable;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.function.MultiplyConverter;
import tec.uom.se.unit.TransformedUnit;
import tec.uom.se.unit.Units;

public enum NIBPVariable implements Variable<NIBPVariable> {
  PRESSURE {
    @Override
    public Unit<?> getUnit() {
      return MM_HG;
    }
  };

  private static final Unit<Pressure> MM_HG = new TransformedUnit<>(Units.PASCAL, new MultiplyConverter(0.00750062));

  static {
    SimpleUnitFormat.getInstance().label(MM_HG, "mm Hg");
  }
}
