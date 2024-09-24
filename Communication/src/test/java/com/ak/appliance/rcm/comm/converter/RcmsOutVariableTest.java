package com.ak.appliance.rcm.comm.converter;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RcmsOutVariableTest {
  @ParameterizedTest
  @EnumSource(RcmsOutVariable.class)
  void testInputVariables(DependentVariable<RcmOutVariable, RcmsOutVariable> variable) {
    assertAll(variable.toString(),
        () -> assertThat(variable.getInputVariablesClass()).isEqualTo(RcmOutVariable.class),
        () -> assertThat(variable.getInputVariables()).hasSize(1),
        () -> assertThat(variable.getUnit()).isEqualTo(RcmOutVariable.valueOf(variable.name()).getUnit())
    );
  }

  @ParameterizedTest
  @EnumSource(value = RcmsOutVariable.class, names = "QS_1")
  void testOptionsEmpty(Variable<RcmsOutVariable> variable) {
    assertThat(variable.options()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = RcmsOutVariable.class, names = "QS_2")
  void testOptionsContains(Variable<RcmsOutVariable> variable) {
    assertThat(variable.options()).contains(Variable.Option.TEXT_VALUE_BANNER);
  }

  @ParameterizedTest
  @EnumSource(value = RcmsOutVariable.class, names = {"QS_1", "QS_2"}, mode = EnumSource.Mode.EXCLUDE)
  void testOptionsNotContains(Variable<RcmsOutVariable> variable) {
    assertThat(variable.options()).isNotEmpty().doesNotContain(Variable.Option.TEXT_VALUE_BANNER);
  }
}