package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("purelogic6f0")
public final class PureLogicViewController6F0 extends AbstractPureLogicViewController {
  public PureLogicViewController6F0() {
    super(PureLogicAxisFrequency.F6_0);
  }
}
