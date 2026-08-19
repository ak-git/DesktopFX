package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Controller
@Profile("purelogic0f5")
public final class PureLogic0F5ViewController extends AbstractPureLogicViewController {
  public PureLogic0F5ViewController() {
    super(PureLogicAxisFrequency.F0_5);
  }
}
