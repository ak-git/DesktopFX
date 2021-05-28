package com.ak.comm.converter;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum ADCVariable implements Variable<ADCVariable> {
  ADC,
  ADC_FILTER {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(8).build();
    }
  }
}
