package com.ak.appliance.suntech.comm.converter;

import com.ak.appliance.suntech.comm.bytes.NIBPResponse;
import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

public final class NIBPConverter extends AbstractConverter<NIBPResponse, NIBPVariable> {
  public static final int FREQUENCY = 125;
  private final int[] out = new int[NIBPVariable.values().length];

  public NIBPConverter() {
    super(NIBPVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(NIBPResponse response) {
    out[NIBPVariable.IS_COMPLETED.ordinal()] = 0;
    response.extractPressure(value -> out[NIBPVariable.PRESSURE.ordinal()] = value);
    response.extractData(value -> System.arraycopy(value, 0, out, NIBPVariable.SYS.ordinal(), value.length));
    response.extractIsCompleted(() -> out[NIBPVariable.IS_COMPLETED.ordinal()] = 1);
    return Stream.of(out);
  }
}
