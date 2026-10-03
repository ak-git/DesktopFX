package com.ak.appliance.caliper.comm.converter;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tech.units.indriya.AbstractUnit;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CaliperVariableTest {
  @Test
  void options() {
    assertThat(EnumSet.allOf(CaliperVariable.class).stream().flatMap(v -> v.options().stream()))
        .isEqualTo(
            List.of(
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = CaliperVariable.class)
  void filterDelay(Variable<CaliperVariable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void unit() {
    assertThat(EnumSet.allOf(CaliperVariable.class).stream().map(Variable::getUnit).collect(Collectors.toSet()))
        .isEqualTo(Set.of(AbstractUnit.ONE));
  }
}