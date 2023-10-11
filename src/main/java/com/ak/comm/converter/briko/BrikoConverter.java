package com.ak.comm.converter.briko;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.ak.comm.converter.briko.BrikoStage2Variable.FREQUENCY;
import static java.lang.Integer.BYTES;

public final class BrikoConverter extends AbstractConverter<BufferFrame, BrikoStage1Variable> {
  public BrikoConverter() {
    super(BrikoStage1Variable.class, FREQUENCY);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull BufferFrame frame) {
    var values = new int[variables().size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = frame.getInt(2 + i * (1 + BYTES) + 1);
    }
    return Stream.of(values);
  }
}
