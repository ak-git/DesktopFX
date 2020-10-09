package com.ak.comm.converter.suntech;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Named;

import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.context.annotation.Profile;

@Named
@Profile("suntech")
public final class NIBPConverter extends AbstractConverter<NIBPResponse, NIBPVariable> {
  public NIBPConverter() {
    super(NIBPVariable.class, 10);
  }

  @Override
  protected Stream<int[]> innerApply(@Nonnull NIBPResponse response) {
    return response.extractPressure().mapToObj(value -> new int[] {value});
  }
}
