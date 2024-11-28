package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("purelogic8f0")
public final class PureLogicViewController8F0 extends AbstractPureLogicViewController {
  public PureLogicViewController8F0() {
    super(PureLogicAxisFrequency.F8_0);
  }
}