package com.ak.numbers;

import com.ak.util.Strings;

import javax.json.JsonObject;
import java.util.stream.Collectors;

public interface TestCoefficients extends Coefficients {
  @Override
  default String readJSON(JsonObject object) {
    return object.getJsonObject(name() + ", x : y").entrySet().stream()
        .map(entry -> String.join(Strings.TAB, entry.getKey(), entry.getValue().toString()))
        .collect(Collectors.joining(Strings.NEW_LINE));
  }

  String name();
}
