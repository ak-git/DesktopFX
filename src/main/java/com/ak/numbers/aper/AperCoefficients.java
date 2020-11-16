package com.ak.numbers.aper;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

public enum AperCoefficients implements Coefficients {
  ADC_TO_OHM {
    @Override
    public String readJSON(@Nonnull JsonObject object) {
      return object.getJsonObject("Current-carrying electrodes, Ohm : ADC").entrySet().stream()
          .map(entry -> String.join(Strings.TAB, entry.getValue().toString(), entry.getKey()))
          .collect(Collectors.joining(Strings.NEW_LINE));
    }
  }
}
