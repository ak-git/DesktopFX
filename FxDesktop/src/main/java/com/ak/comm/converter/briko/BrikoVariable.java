package com.ak.comm.converter.briko;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.unit.Units;

public enum BrikoVariable implements Variable<BrikoVariable> {
  C1 {
    @Override
    public Unit<?> getUnit() {
      return Units.GRAM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> operand -> (int) ((2.0e-5 * operand - 0.4809) * 1000.0)).build();
    }
  }, C2, C3
}
