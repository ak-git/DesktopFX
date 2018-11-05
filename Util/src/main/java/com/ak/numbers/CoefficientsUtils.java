package com.ak.numbers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

import javax.annotation.Nonnull;

enum CoefficientsUtils {
  ;

  static double[] read(@Nonnull Scanner scanner) {
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
}
