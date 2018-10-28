package com.ak.numbers;

import java.util.Scanner;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonObject;

import com.ak.util.Strings;

public interface Coefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    Scanner scanner = new Scanner(readJSON(Json.createReader(
        getClass().getResourceAsStream(String.format("%s.json", getClass().getPackageName().replaceFirst(".*\\.", "")))
    ).readObject()));
    return CoefficientsUtils.read(scanner);
  }

  default double[][] getPairs() {
    double[] xAndY = get();
    double[][] pairs = new double[xAndY.length / 2][2];

    for (int i = 0; i < pairs.length; i++) {
      pairs[i][0] = xAndY[i * 2];
      pairs[i][1] = xAndY[i * 2 + 1];
    }
    return pairs;
  }

  String name();

  default String readJSON(@Nonnull JsonObject object) {
    return Strings.EMPTY;
  }
}
