package com.ak.digitalfilter;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

import javax.inject.Provider;

public interface Coefficients extends Provider<double[]> {
  @Override
  default double[] get() {
    Scanner scanner = new Scanner(getClass().getResourceAsStream(String.format("%s.txt", name().toLowerCase())),
        Charset.defaultCharset().name());
    scanner.useLocale(Locale.ROOT);

    Collection<Double> coeffs = new LinkedList<>();
    while (scanner.hasNextDouble()) {
      coeffs.add(scanner.nextDouble());
    }
    return coeffs.stream().mapToDouble(Double::doubleValue).toArray();
  }

  String name();
}
