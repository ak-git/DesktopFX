package com.ak.numbers.rcm;

import java.util.stream.Collectors;

import com.ak.numbers.Coefficients;
import com.ak.util.Strings;

public enum RcmCoefficients {
  CC_ADC_TO_OHM {
    @Override
    public Coefficients of(int channelNumber) {
      return object -> object.getJsonObject("Current-carrying electrodes, Ohm : ADC[Channel-1, Channel-2]").entrySet().stream()
          .map(entry -> String.format("%s\t%s", entry.getValue().asJsonArray().getInt(channelNumber - 1), entry.getKey()))
          .collect(Collectors.joining(Strings.NEW_LINE));
    }
  },
  RHEO_ADC_TO_260_MILLI {
    @Override
    public Coefficients of(int channelNumber) {
      return object -> {
        String channel = String.format("Channel-%s", channelNumber);
        return object.getJsonObject("Rheo 0.26 Ohm : ADC{CurrentCarrying, Rheo}").getJsonObject(channel).entrySet().stream()
            .map(entry -> String.format("%s\t%s", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(Strings.NEW_LINE));
      };
    }
  };

  public abstract Coefficients of(int channelNumber);
}
