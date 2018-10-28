package com.ak.numbers;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Supplier;

public interface SimpleCoefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    InputStream resourceAsStream = getClass().getResourceAsStream(String.format("%s.txt", name()));
    Scanner scanner = new Scanner(resourceAsStream, Charset.defaultCharset().name());
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

  String name();
}
