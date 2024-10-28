package com.ak.appliance.sktbpr.comm.converter;

import com.ak.appliance.sktbpr.comm.bytes.SKTBResponse;
import com.ak.comm.converter.AbstractConverter;

import java.util.stream.Stream;

public final class SKTBConverter extends AbstractConverter<SKTBResponse, SKTBVariable> {

  public static final int FREQUENCY = 20;

  public SKTBConverter() {
    super(SKTBVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(SKTBResponse frame) {
    return Stream.of(new int[] {frame.rotateAngle(), frame.flexAngle()});
  }
}
