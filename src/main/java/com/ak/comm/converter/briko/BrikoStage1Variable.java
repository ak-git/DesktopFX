package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

import javax.measure.Unit;

import static com.ak.util.Strings.ANGLE;
import static tec.uom.se.unit.Units.RADIAN;

public enum BrikoStage1Variable implements Variable<BrikoStage1Variable> {
  FORCE1,
  FORCE2,
  FORCE3,
  FORCE4,
  ENCODER1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().angle().build();
    }

    @Override
    public Unit<?> getUnit() {
      return RADIAN.alternate(ANGLE).divide(1000.0);
    }
  },
  ENCODER2
}
