package com.ak.numbers;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.json.JsonObject;

import com.ak.util.Strings;

enum InterpolatorCoefficients implements Coefficients {
  INTERPOLATOR_TEST_AKIMA, INTERPOLATOR_TEST_LINEAR, INTERPOLATOR_TEST_INVALID;

  @Override
  public final String readJSON(@Nonnull JsonObject object) {
    return object.getJsonObject(name() + ", x : y").entrySet().stream()
        .map(entry -> String.format("%s\t%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }
}
