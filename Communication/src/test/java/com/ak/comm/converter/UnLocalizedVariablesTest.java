package com.ak.comm.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnLocalizedVariablesTest {
  @Test
  void testToString() {
    assertThat(Variables.toString(UnLocalizedVariables.MISSING_RESOURCE))
        .isEqualTo(UnLocalizedVariables.MISSING_RESOURCE.name());
  }
}