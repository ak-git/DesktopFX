package com.ak.numbers.aper;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.ak.numbers.Coefficients;
import com.ak.util.Metrics;
import com.ak.util.Strings;

public enum AperCoefficients implements Coefficients {
  RHEO, MYO, ECG,
  ADC_TO_OHM_1 {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return readCurrentCarryingCalibration(object, 0);
    }
  },
  ADC_TO_OHM_2 {
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

  static String readPotentialUnitCalibration(@Nonnull JsonObject object, @Nonnull Coefficients coefficients) {
    String ohms = String.format(Locale.ROOT, "%.1f", Metrics.fromMilli(Double.parseDouble(Strings.numberSuffix(coefficients.name()))));
    String channel = String.format("Channel-%s", Strings.numberSuffix(coefficients.getClass().getName()));
    Set<Map.Entry<String, JsonValue>> entries = object.getJsonObject("Potential-unit electrodes, Ohm : ADC[CurrentCarrying, PotentialUnit]").
        getJsonObject(ohms).getJsonObject(channel).entrySet();
    return entries.stream()
        .map(entry -> String.format("%s\t%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
