package com.ak.numbers.rcm;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.numbers.common.CommonCoefficients;

public enum RcmBaseSurfaceCoefficientsChannel2 implements Coefficients {
  CCU_VADC_0, CCU_VADC_15100, CCU_VADC_30200, CCU_VADC_90400, CCU_VADC_211000;

  @Override
  public final String readJSON(@Nonnull JsonObject object) {
    return CommonCoefficients.readBaseCalibration(object, this);
  }
}
