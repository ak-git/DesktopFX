package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("purelogic0f5")
public final class PureLogicViewController0F5 extends AbstractPureLogicViewController {
  public PureLogicViewController0F5() {
    super(PureLogicAxisFrequency.F0_5);
  }
}
