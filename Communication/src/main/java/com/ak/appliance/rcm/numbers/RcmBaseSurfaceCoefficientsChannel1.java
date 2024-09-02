package com.ak.appliance.rcm.numbers;

import com.ak.numbers.Coefficients;
import com.ak.numbers.common.CommonCoefficients;

import javax.json.JsonObject;

public enum RcmBaseSurfaceCoefficientsChannel1 implements Coefficients {
  CCU_VADC_0, CCU_VADC_15148, CCU_VADC_30129, CCU_VADC_90333, CCU_VADC_211000;

  @Override
  public final String readJSON(JsonObject object) {
    return CommonCoefficients.readBaseCalibration(object, this);
  }
}
