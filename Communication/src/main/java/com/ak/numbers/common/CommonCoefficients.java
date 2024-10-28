package com.ak.numbers.common;

import com.ak.numbers.Coefficients;
import com.ak.util.Metrics;
import com.ak.util.Strings;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.units.indriya.unit.Units.METRE;

public enum CommonCoefficients implements SimpleCoefficients {
  MYO, ECG;

  public static <C extends Enum<C> & Coefficients> String readPotentialUnitCalibration(JsonObject object, C coefficients) {
    return readCalibration(object, coefficients, "Potential-unit electrodes, Ohm : ADC[CurrentCarrying, PotentialUnit]");
  }

  public static <C extends Enum<C> & Coefficients> String readBaseCalibration(JsonObject object, C coefficients) {
    return readCalibration(object, coefficients, "Potential-unit electrodes, Ohm : ADC{CurrentCarrying, Base}");
  }

  private static <C extends Enum<C> & Coefficients> String readCalibration(JsonObject object, C coefficients, String name) {
    double mm = Double.parseDouble(Strings.numberSuffix(coefficients.name()));
    var ohms = String.format(Locale.ROOT, "%.3f", Metrics.Length.MILLI.to(mm, METRE));
    String channel = MessageFormat.format("Channel-{0}", Strings.numberSuffix(coefficients.getClass().getName()));
    Set<Map.Entry<String, JsonValue>> entries = object.getJsonObject(Objects.requireNonNull(name)).getJsonObject(ohms).getJsonObject(channel).entrySet();
    return entries.stream().map(entry -> String.join(Strings.TAB, entry.getKey(), entry.getValue().toString())).collect(Collectors.joining(Strings.NEW_LINE));
  }
}
