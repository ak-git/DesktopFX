package com.ak.appliance.suntech.comm.converter;

import com.ak.comm.converter.Variable;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.unit.TransformedUnit;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import java.util.Collections;
import java.util.Set;

import static tech.units.indriya.function.AbstractConverter.IDENTITY;

public enum NIBPVariable implements Variable<NIBPVariable> {
  PRESSURE {
    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
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
  },
  IS_COMPLETED {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  };

  private static final Unit<Dimensionless> MM_HG = new TransformedUnit<>(AbstractUnit.ONE, IDENTITY);

  @Override
  public Unit<?> getUnit() {
    return MM_HG;
  }

  static {
    SimpleUnitFormat.getInstance().label(MM_HG, "mmHg");
  }
}
