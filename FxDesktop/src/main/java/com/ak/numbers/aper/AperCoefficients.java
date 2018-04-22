package com.ak.numbers.aper;

import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

public enum AperCoefficients implements Coefficients {
  RHEO, MYO, ECG,
  ADC_TO_OHM_1 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 0);
    }
  }, ADC_TO_OHM_2 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 1);
    }
  };

  static String readCurrentCarryingCalibration(@Nonnull JsonObject object, @Nonnegative int channelNumber) {
    return object.getJsonObject("Current-carrying electrodes, Ohm : ADC[Channel-1, Channel-2]").entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getValue().asJsonArray().getInt(channelNumber), entry.getKey()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
