package com.ak.numbers.aper.sincos;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.ak.numbers.Coefficients;
import com.ak.util.Metrics;
import com.ak.util.Strings;

import static com.ak.numbers.common.CommonCoefficients.readCurrentCarryingCalibration;

public enum AperCoefficients implements Coefficients {
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
