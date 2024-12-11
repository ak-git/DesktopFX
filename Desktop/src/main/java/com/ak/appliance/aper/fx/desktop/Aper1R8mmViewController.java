package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperStage5Current1Variable;
import com.ak.appliance.aper.comm.converter.AperStage6Current1Variable8mm;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ak.appliance.aper.fx.desktop.Aper1MyoViewController.converterAper1Myo;

@Component
@Profile("aper1-R2-8mm")
public final class Aper1R8mmViewController extends AbstractAperViewController<AperStage6Current1Variable8mm> {
  public Aper1R8mmViewController() {
    super(() -> converterAper1Myo().chainInstance(AperStage5Current1Variable.class).chainInstance(AperStage6Current1Variable8mm.class));
  }
}
