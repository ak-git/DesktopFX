package com.ak.numbers.rcm;

import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

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
  };

  String readRheo260Calibration(@Nonnull JsonObject object) {
    String channel = String.format("Channel-%s", Strings.numberSuffix(name()));
    return object.getJsonObject("Rheo 0.26 Ohm : ADC{CurrentCarrying, Rheo}").getJsonObject(channel).entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }

  private static String readCurrentCarryingCalibration(@Nonnull JsonObject object, @Nonnegative int channelNumber) {
    return object.getJsonObject("Current-carrying electrodes, Ohm : ADC[Channel-1, Channel-2]").entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getValue().asJsonArray().getInt(channelNumber), entry.getKey()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
