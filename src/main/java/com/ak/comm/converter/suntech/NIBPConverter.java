package com.ak.comm.converter.suntech;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.AbstractConverter;

public final class NIBPConverter extends AbstractConverter<NIBPResponse, NIBPVariable> {
  public static final int FREQUENCY = 100;
  private final int[] out = new int[NIBPVariable.values().length];

  public NIBPConverter() {
    super(NIBPVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull NIBPResponse response) {
    out[NIBPVariable.IS_COMPLETED.ordinal()] = 0;
    response.extractPressure(value -> out[NIBPVariable.PRESSURE.ordinal()] = value);
    response.extractData(value -> System.arraycopy(value, 0, out, NIBPVariable.SYS.ordinal(), value.length));
    response.extractIsCompleted(() -> out[NIBPVariable.IS_COMPLETED.ordinal()] = 1);
    return Stream.of(out);
  }
}
