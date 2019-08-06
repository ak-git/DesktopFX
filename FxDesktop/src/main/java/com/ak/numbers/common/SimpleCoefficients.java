package com.ak.numbers.common;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.function.Supplier;

import com.ak.numbers.CoefficientsUtils;

public interface SimpleCoefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    InputStream resourceAsStream = getClass().getResourceAsStream(String.format("%s.txt", name()));
    Scanner scanner = new Scanner(resourceAsStream, Charset.defaultCharset().name());
    return CoefficientsUtils.read(scanner);
  }

  String name();
}
