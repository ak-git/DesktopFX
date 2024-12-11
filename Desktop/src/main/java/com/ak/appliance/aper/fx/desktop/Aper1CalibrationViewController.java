package com.ak.appliance.aper.fx.desktop;

import com.ak.appliance.aper.comm.converter.AperCalibrationCurrent1Variable;
import com.ak.appliance.aper.comm.converter.AperStage1Variable;
import com.ak.comm.converter.LinkedConverter;
import com.ak.comm.converter.ToIntegerConverter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aper1-calibration")
public final class Aper1CalibrationViewController extends AbstractAperViewController<AperCalibrationCurrent1Variable> {
  public Aper1CalibrationViewController() {
    super(() -> LinkedConverter.of(new ToIntegerConverter<>(AperStage1Variable.class, 1000),
        AperCalibrationCurrent1Variable.class));
  }
}
