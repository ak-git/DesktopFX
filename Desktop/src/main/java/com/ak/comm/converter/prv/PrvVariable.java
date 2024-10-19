package com.ak.comm.converter.prv;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum PrvVariable implements Variable<PrvVariable> {
  ADC,
  ADC_SMOOTH {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(8).build();
    }
  }
}
