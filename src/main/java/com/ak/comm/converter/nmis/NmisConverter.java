package com.ak.comm.converter.nmis;

import java.util.stream.Stream;

import javax.inject.Named;

import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.converter.AbstractConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Named
@Profile("nmis")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class NmisConverter extends AbstractConverter<NmisResponseFrame, NmisVariable> {
  public NmisConverter() {
    super(NmisVariable.class, 200);
  }

  @Override
  protected Stream<int[]> innerApply(NmisResponseFrame frame) {
    return frame.extractTime().mapToObj(value -> new int[] {value});
  }
}
