package com.ak.comm.converter.sktbpr;

import com.ak.comm.bytes.sktbpr.SKTBResponse;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

@Component
@Profile("sktb-pr")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class SKTBConverter extends AbstractConverter<SKTBResponse, SKTBVariable> {

  public static final int FREQUENCY = 10;

  public SKTBConverter() {
    super(SKTBVariable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull SKTBResponse frame) {
    return Stream.of(new int[] {frame.rotateAngle(), frame.flexAngle()});
  }
}
