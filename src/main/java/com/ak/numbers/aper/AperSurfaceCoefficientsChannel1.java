package com.ak.numbers.aper;

import com.ak.numbers.Coefficients;
import com.ak.numbers.common.CommonCoefficients;

import javax.json.JsonObject;

public enum AperSurfaceCoefficientsChannel1 implements Coefficients {
  CCU_VADC_0, CCU_VADC_15148, CCU_VADC_30129, CCU_VADC_90333, CCU_VADC_330990;

  @Override
  public final String readJSON(JsonObject object) {
    return CommonCoefficients.readPotentialUnitCalibration(object, this);
  }
}
