package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperStage5Current1Variable;
import com.ak.appliance.aper.comm.converter.AperStage6Current1Variable6mm;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ak.appliance.aper.fx.desktop.Aper1MyoViewController.converterAper1Myo;

@Component
@Profile("aper1-R2-6mm")
public final class Aper1R6mmViewController extends AbstractAperViewController<AperStage6Current1Variable6mm> {
  public Aper1R6mmViewController() {
    super(() -> converterAper1Myo().chainInstance(AperStage5Current1Variable.class).chainInstance(AperStage6Current1Variable6mm.class));
  }
}
