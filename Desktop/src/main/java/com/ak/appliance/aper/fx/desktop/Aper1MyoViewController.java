package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperStage1Variable;
import com.ak.appliance.aper.comm.converter.AperStage2UnitsVariable;
import com.ak.appliance.aper.comm.converter.AperStage3Variable;
import com.ak.appliance.aper.comm.converter.AperStage4Current1Variable;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aper1-myo")
public final class Aper1MyoViewController extends AbstractAperViewController<AperStage4Current1Variable> {
  public Aper1MyoViewController() {
    super(Aper1MyoViewController::converterAper1Myo);
  }

  static LinkedConverter<BufferFrame, AperStage3Variable, AperStage4Current1Variable> converterAper1Myo() {
    return LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
        .chainInstance(AperStage3Variable.class).chainInstance(AperStage4Current1Variable.class);
  }
}
