package com.ak.appliance.sktbpr.comm.converter;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class SKTBVariableTest {
  @ParameterizedTest
  @EnumSource(value = SKTBVariable.class)
  void testVariables(Variable<SKTBVariable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
    assertThat(variable.getUnit()).hasToString("°");
  }
}