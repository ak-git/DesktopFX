package com.ak.numbers.common;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.ak.numbers.Coefficients;
import com.ak.numbers.SimpleCoefficients;
import com.ak.util.Metrics;
import com.ak.util.Strings;

public enum CommonCoefficients implements SimpleCoefficients {
  MYO, ECG;

  public static <C extends Enum<C> & Coefficients> String readPotentialUnitCalibration(@Nonnull JsonObject object, @Nonnull C coefficients) {
    return readCalibration(object, coefficients, "Potential-unit electrodes, Ohm : ADC[CurrentCarrying, PotentialUnit]");
  }

  public static <C extends Enum<C> & Coefficients> String readBaseCalibration(@Nonnull JsonObject object, @Nonnull C coefficients) {
    return readCalibration(object, coefficients, "Potential-unit electrodes, Ohm : ADC{CurrentCarrying, Base}");
  }

  private static <C extends Enum<C> & Coefficients> String readCalibration(@Nonnull JsonObject object, @Nonnull C coefficients, @Nonnull String name) {
    String ohms = String.format(Locale.ROOT, "%.1f", Metrics.fromMilli(Double.parseDouble(Strings.numberSuffix(coefficients.name()))));
    String channel = String.format("Channel-%s", Strings.numberSuffix(coefficients.getClass().getName()));
    Set<Map.Entry<String, JsonValue>> entries = object.getJsonObject(name).getJsonObject(ohms).getJsonObject(channel).entrySet();
    return entries.stream().map(entry -> String.format("%s\t%s", entry.getKey(), entry.getValue())).collect(Collectors.joining(Strings.NEW_LINE));
  }
}
