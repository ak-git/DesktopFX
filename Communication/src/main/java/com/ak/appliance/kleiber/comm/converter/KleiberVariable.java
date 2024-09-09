package com.ak.appliance.kleiber.comm.converter;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

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
      return FilterBuilder.of().operator(() -> x -> {
        int mV = x * 10;
        if (mV == NO_CONNECT) {
          mV = 0;
        }
        return mV;
      }).build();
    }
  }, M2, M3, M4, M5, M6, M7, M8;

  private static final int NO_CONNECT = -1250;
}
