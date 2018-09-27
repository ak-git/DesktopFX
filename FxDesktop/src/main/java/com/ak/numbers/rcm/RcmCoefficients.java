package com.ak.numbers.rcm;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;

import static com.ak.numbers.aper.AperCoefficients.readCurrentCarryingCalibration;

public enum RcmCoefficients implements Coefficients {
  ADC_TO_OHM_1 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 0);
    }
  },
  ADC_TO_OHM_2 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 1);
    }
  }
}
