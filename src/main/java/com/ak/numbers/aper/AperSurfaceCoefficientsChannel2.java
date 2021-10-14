package com.ak.numbers.aper;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.numbers.common.CommonCoefficients;

public enum AperSurfaceCoefficientsChannel2 implements Coefficients {
  CCU_VADC_0, CCU_VADC_15148, CCU_VADC_30129, CCU_VADC_90333, CCU_VADC_330990;

  @Override
  public final String readJSON(@Nonnull JsonObject object) {
    return CommonCoefficients.readPotentialUnitCalibration(object, this);
  }
}
