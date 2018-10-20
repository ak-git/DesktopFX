package com.ak.numbers.rcm;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

import static com.ak.numbers.common.CommonCoefficients.readCurrentCarryingCalibration;

public enum RcmCoefficients implements Coefficients {
  CC_ADC_TO_OHM_1 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 0);
    }
  },
  CC_ADC_TO_OHM_2 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 1);
    }
  },
  RHEO_ADC_TO_260_MILLI_1 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readRheo260Calibration(object);
    }
  },
  RHEO_ADC_TO_260_MILLI_2 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readRheo260Calibration(object);
    }
  },
  BR_F200, BR_F025, BR_F005;

  String readRheo260Calibration(@Nonnull JsonObject object) {
    String channel = String.format("Channel-%s", Strings.numberSuffix(name()));
    return object.getJsonObject("Rheo 0.26 Ohm : ADC{CurrentCarrying, Rheo}").getJsonObject(channel).entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
