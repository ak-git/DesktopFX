package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("purelogic2f0")
public final class PureLogicViewController2F0 extends AbstractPureLogicViewController {
  public PureLogicViewController2F0() {
    super(PureLogicAxisFrequency.F2_0);
  }
}
