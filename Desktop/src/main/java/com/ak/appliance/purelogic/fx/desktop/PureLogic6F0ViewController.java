package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Controller
@Profile("purelogic6f0")
public final class PureLogic6F0ViewController extends AbstractPureLogicViewController {
  public PureLogic6F0ViewController() {
    super(PureLogicAxisFrequency.F6_0);
  }
}
