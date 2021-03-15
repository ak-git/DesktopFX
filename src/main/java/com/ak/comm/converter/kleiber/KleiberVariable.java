package com.ak.comm.converter.kleiber;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

/**
 * <p>
 * Info about Kleiber Myo
 * <pre>
 *   0xAA, <b>Float-1 (4 bytes) ... Float-8 (4 bytes)</b>, 0xBB
 * </pre>
 * Total 34 bytes
 */
public enum KleiberVariable implements Variable<KleiberVariable> {
  M1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> x * 10).build();
    }
  }, M2, M3, M4, M5, M6, M7, M8
}
