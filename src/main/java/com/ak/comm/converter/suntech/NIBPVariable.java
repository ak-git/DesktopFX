package com.ak.comm.converter.suntech;

import java.util.Collections;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;

import com.ak.comm.converter.Variable;
import tec.uom.se.AbstractUnit;
import tec.uom.se.format.SimpleUnitFormat;
import tec.uom.se.unit.TransformedUnit;
import tec.uom.se.unit.Units;

import static tec.uom.se.AbstractConverter.IDENTITY;

public enum NIBPVariable implements Variable<NIBPVariable> {
  PRESSURE,
  SYS {
    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  DIA {
    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  PULSE {
    @Override
    public Unit<?> getUnit() {
      return AbstractUnit.ONE.divide(Units.MINUTE);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  MEAN {
    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  private static final Unit<Dimensionless> MM_HG = new TransformedUnit<>(AbstractUnit.ONE, IDENTITY);

  @Override
  public Unit<?> getUnit() {
    return MM_HG;
  }

  static {
    SimpleUnitFormat.getInstance().label(MM_HG, "mm Hg");
  }
}
