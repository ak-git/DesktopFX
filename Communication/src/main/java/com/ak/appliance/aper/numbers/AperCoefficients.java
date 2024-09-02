package com.ak.appliance.aper.numbers;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

import javax.json.JsonObject;
import java.util.stream.Collectors;

public enum AperCoefficients implements Coefficients {
  ADC_TO_OHM {
    @Override
    public String readJSON(JsonObject object) {
      return object.getJsonObject("Current-carrying electrodes, Ohm : ADC").entrySet().stream()
          .map(entry -> String.join(Strings.TAB, entry.getValue().toString(), entry.getKey()))
          .collect(Collectors.joining(Strings.NEW_LINE));
    }
  }
}
