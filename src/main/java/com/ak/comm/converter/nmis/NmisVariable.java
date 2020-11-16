package com.ak.comm.converter.nmis;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

/**
 * <p>
 * Info about Neuro-Muscular Test Stand format time
 * <pre>
 *   0х7Е, 0х45 (address for wrapped frame type), Len, CounterLow, CounterHi, DATA_WRAPPED_RSC_Energia ..., CRC
 * </pre>
 * Examples:
 * <pre>
 *   NmisResponseFrame[ 0x7e 0x45 0x02 <b>0x80 0x00</b> 0x45 ] DATA
 *   NmisResponseFrame[ 0x7e 0x45 0x09 <b>0x85 0x00</b> 0x01 0x05 0x0b 0xe0 0xb1 0xe1 0x7a 0x4e ] DATA
 * </pre>
 * each 5 ms.
 */
public enum NmisVariable implements Variable<NmisVariable> {
  COUNTER_5_MILLI_SECONDS {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.SECOND);
    }
  }
}
