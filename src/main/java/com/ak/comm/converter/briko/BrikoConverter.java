package com.ak.comm.converter.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static java.lang.Integer.BYTES;

@Component("briko-converter")
@Profile("briko")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class BrikoConverter extends AbstractConverter<BufferFrame, BrikoVariable> {
  public BrikoConverter() {
    super(BrikoVariable.class, 1000);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = frame.getInt(2 + i * (1 + BYTES));
    }
    return Stream.of(values);
  }
}
