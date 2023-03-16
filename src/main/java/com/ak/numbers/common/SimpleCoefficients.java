package com.ak.numbers.common;

import com.ak.numbers.Coefficients;
import com.ak.util.Extension;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

public interface SimpleCoefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream(Extension.TXT.attachTo(name())));
    var scanner = new Scanner(resourceAsStream, Charset.defaultCharset());
    return Coefficients.read(scanner);
  }

  String name();
}
