package com.ak.numbers;

import com.ak.util.Extension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class CoefficientsUtilsTest {
  @Test
  void testSerialize() {
    double[] out = CoefficientsUtils.serialize(new double[] {1.0, -1.0, 3.0, -3.0}, new double[] {1.0, -1.0}, 5);
    assertThat(out).containsExactly(new double[] {1.0, 0.0, 3.0, 0.0, 0.0}, byLessThan(1.0e-3));
  }

  @Test
  void testRead() throws IOException {
    try (InputStream resourceAsStream = getClass().getResourceAsStream(Extension.TXT.attachTo("DIFF"))) {
      Scanner scanner = new Scanner(Objects.requireNonNull(resourceAsStream), Charset.defaultCharset());
      assertThat(CoefficientsUtils.read(scanner)).containsExactly(-1.0, 0.0, 1.0);
    }
  }
}