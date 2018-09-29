package com.ak.numbers.common;

import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

public enum CommonCoefficients implements Coefficients {
  RHEO, MYO, ECG;

  public static String readCurrentCarryingCalibration(@Nonnull JsonObject object) {
    return object.getJsonObject("Current-carrying electrodes, Ohm : ADC").entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getValue().toString(), entry.getKey()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }

  public static String readCurrentCarryingCalibration(@Nonnull JsonObject object, @Nonnegative int channelNumber) {
    return object.getJsonObject("Current-carrying electrodes, Ohm : ADC[Channel-1, Channel-2]").entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getValue().asJsonArray().getInt(channelNumber), entry.getKey()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
