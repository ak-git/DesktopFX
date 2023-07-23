package com.ak.comm.converter.sktbpr;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

class SKTBVariableTest {
  @ParameterizedTest
  @EnumSource(value = SKTBVariable.class)
  void testVariables(@Nonnull Variable<SKTBVariable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
    assertThat(variable.getUnit()).hasToString("°");
  }
}