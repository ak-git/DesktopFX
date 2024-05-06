package com.ak.numbers;

import com.ak.util.Strings;

import javax.json.JsonObject;
import java.util.stream.Collectors;

enum InterpolatorCoefficients implements Coefficients {
  INTERPOLATOR_TEST_AKIMA, INTERPOLATOR_TEST_LINEAR, INTERPOLATOR_TEST_INVALID;

  @Override
  public final String readJSON(JsonObject object) {
    return object.getJsonObject(name() + ", x : y").entrySet().stream()
        .map(entry -> String.join(Strings.TAB, entry.getKey(), entry.getValue().toString()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
