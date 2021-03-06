package com.ak.numbers.common;

import java.text.MessageFormat;
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
    String channel = MessageFormat.format("Channel-{0}", Strings.numberSuffix(coefficients.getClass().getName()));
    Set<Map.Entry<String, JsonValue>> entries = object.getJsonObject(name).getJsonObject(ohms).getJsonObject(channel).entrySet();
    return entries.stream().map(entry -> String.join(Strings.TAB, entry.getKey(), entry.getValue().toString())).collect(Collectors.joining(Strings.NEW_LINE));
  }
}
