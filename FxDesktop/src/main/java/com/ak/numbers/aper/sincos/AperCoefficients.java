package com.ak.numbers.aper.sincos;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;

import static com.ak.numbers.common.CommonCoefficients.readCurrentCarryingCalibration;

public enum AperCoefficients implements Coefficients {
  ADC_TO_OHM {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object);
    }
  }
}
