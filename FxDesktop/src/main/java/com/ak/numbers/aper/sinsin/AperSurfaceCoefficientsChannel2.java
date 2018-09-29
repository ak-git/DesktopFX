package com.ak.numbers.aper.sinsin;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.numbers.common.CommonCoefficients;

public enum AperSurfaceCoefficientsChannel2 implements Coefficients {
  CCU_VADC_0, CCU_VADC_15100, CCU_VADC_30200, CCU_VADC_90400, CCU_VADC_301400;

  @Override
  public final String readJSON(@Nonnull JsonObject object) {
    return CommonCoefficients.readPotentialUnitCalibration(object, this);
  }
}
