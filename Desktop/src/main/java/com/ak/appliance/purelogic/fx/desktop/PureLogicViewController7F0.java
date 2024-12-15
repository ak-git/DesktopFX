package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("purelogic7f0")
public final class PureLogicViewController7F0 extends AbstractPureLogicViewController {
  public PureLogicViewController7F0() {
    super(PureLogicAxisFrequency.F7_0);
  }
}
