package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("purelogic5f0")
public final class PureLogicViewController5F0 extends AbstractPureLogicViewController {
  public PureLogicViewController5F0() {
    super(PureLogicAxisFrequency.F5_0);
  }
}
