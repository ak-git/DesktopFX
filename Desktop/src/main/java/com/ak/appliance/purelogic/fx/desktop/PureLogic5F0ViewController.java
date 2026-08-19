package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Controller
@Profile("purelogic5f0")
public final class PureLogic5F0ViewController extends AbstractPureLogicViewController {
  public PureLogic5F0ViewController() {
    super(PureLogicAxisFrequency.F5_0);
  }
}
