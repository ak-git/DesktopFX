package com.ak.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionTest {
  static Stream<String> fileNames() {
    return Stream.of("COEFF", "file.test.", ".ignore", ".txt");
  }

  @ParameterizedTest
  @EmptySource
  @MethodSource("fileNames")
  void testAttachTo(String name) {
    for (Extension e : EnumSet.complementOf(EnumSet.of(Extension.NONE))) {
      if (name.toUpperCase().endsWith(e.name())) {
        assertThat(e.attachTo(name)).isEqualTo(".%s", e.name().toLowerCase());
      }
      else {
        assertThat(e.attachTo(name)).isEqualTo(String.join(".", name, e.name().toLowerCase()));
      }
      assertTrue(e.is(e.attachTo(name)), name);
    }
    assertThat(Extension.NONE.attachTo(name)).isEqualTo(name);
  }

  @ParameterizedTest
  @EmptySource
  @MethodSource("fileNames")
  void testReplace(String name) {
    for (Extension e : EnumSet.allOf(Extension.class)) {
      String expected = name;
      if (name.toUpperCase().endsWith(e.name())) {
        expected = name.substring(0, name.lastIndexOf(".%s".formatted(e.name().toLowerCase())));
      }
      assertThat(e.clean(e.attachTo(name))).isEqualTo(expected);
      assertThat(e.clean(name)).isEqualTo(expected);
    }
  }
}