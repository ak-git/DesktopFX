package com.ak.appliance.purelogic.fx.desktop;

import com.ak.appliance.purelogic.comm.converter.PureLogicAxisFrequency;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

@Controller
@Profile("purelogic2f0")
public final class PureLogic2F0ViewController extends AbstractPureLogicViewController {
  public PureLogic2F0ViewController() {
    super(PureLogicAxisFrequency.F2_0);
  }
}
