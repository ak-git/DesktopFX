package com.ak.numbers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import javax.json.Json;
import javax.json.JsonObject;

import com.ak.util.Strings;

public interface Coefficients extends Provider<double[]> {
  @Override
  default double[] get() {
    Scanner scanner = new Scanner(readJSON(Json.createReader(
        getClass().getResourceAsStream(String.format("%s.json", getClass().getPackageName().replaceFirst(".*\\.", "")))
    ).readObject()));
    scanner.useLocale(Locale.ROOT);

    Collection<Double> coeffs = new LinkedList<>();
    while (scanner.hasNext() && !scanner.hasNextDouble()) {
      scanner.next();
    }
    while (scanner.hasNextDouble()) {
      coeffs.add(scanner.nextDouble());
    }
    return coeffs.stream().mapToDouble(Double::doubleValue).toArray();
  }

  default double[][] getPairs() {
    double[] xAndY = get();
    if ((xAndY.length & 1) == 1) {
      throw new IllegalArgumentException(String.format("Number %d of coefficients %s is not even", xAndY.length, name()));
    }

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
