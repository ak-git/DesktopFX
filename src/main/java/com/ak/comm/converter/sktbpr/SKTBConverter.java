package com.ak.comm.converter.sktbpr;

import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.converter.AbstractConverter;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public final class SKTBConverter extends AbstractConverter<SKTBResponse, SKTBVariable> {

  public static final int FREQUENCY = 20;

  public SKTBConverter() {
    super(SKTBVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull SKTBResponse frame) {
    return Stream.of(new int[] {frame.rotateAngle(), frame.flexAngle()});
  }
}
