package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperStage5Current1Variable;
import com.ak.appliance.aper.comm.converter.AperStage6Current1Variable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ak.appliance.aper.fx.desktop.Aper1MyoViewController.converterAper1Myo;

@Component
@Profile("aper1-R1")
public final class Aper1R1ViewController extends AbstractAperViewController<AperStage6Current1Variable> {
  public Aper1R1ViewController() {
    super(() -> converterAper1Myo().chainInstance(AperStage5Current1Variable.class).chainInstance(AperStage6Current1Variable.class));
  }
}
