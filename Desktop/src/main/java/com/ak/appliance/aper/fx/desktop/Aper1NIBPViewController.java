package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperStage1Variable;
import com.ak.appliance.aper.comm.converter.AperStage2UnitsVariable;
import com.ak.appliance.aper.comm.converter.AperStage3Current1NIBPVariable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aper1-nibp")
public final class Aper1NIBPViewController extends AbstractAperViewController<AperStage3Current1NIBPVariable> {
  public Aper1NIBPViewController() {
    super(
        () -> LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
            .chainInstance(AperStage3Current1NIBPVariable.class)
    );
  }
}
