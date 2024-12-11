package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperStage1Variable;
import com.ak.appliance.aper.comm.converter.AperStage2UnitsVariable;
import com.ak.appliance.aper.comm.converter.AperStage3Variable;
import com.ak.appliance.aper.comm.converter.AperStage4Current2Variable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aper2-ecg")
public final class Aper2ECGViewController extends AbstractAperViewController<AperStage4Current2Variable> {
  public Aper2ECGViewController() {
    super(
        () -> LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000), AperStage2UnitsVariable.class)
            .chainInstance(AperStage3Variable.class).chainInstance(AperStage4Current2Variable.class)
    );
  }
}
