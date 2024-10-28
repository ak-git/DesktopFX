package com.ak.appliance.nmi.comm.converter;

import com.ak.comm.converter.Variable;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.Collections;
import java.util.Set;

import static javax.measure.MetricPrefix.CENTI;

public enum NmiVariable implements Variable<NmiVariable> {
  ACC_1 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  ACC_2,
  ACC_3,
  RHEO_1 {
    @Override
    public Unit<?> getUnit() {
      return CENTI(Units.OHM);
    }
  },
  RHEO_2
}
