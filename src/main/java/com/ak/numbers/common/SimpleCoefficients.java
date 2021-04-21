package com.ak.numbers.common;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

import com.ak.numbers.CoefficientsUtils;
import com.ak.util.Extension;

public interface SimpleCoefficients extends Supplier<double[]> {
  @Override
  default double[] get() {
    InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream(Extension.TXT.attachTo(name())));
    var scanner = new Scanner(resourceAsStream, Charset.defaultCharset().name());
    return CoefficientsUtils.read(scanner);
  }

  String name();
}
