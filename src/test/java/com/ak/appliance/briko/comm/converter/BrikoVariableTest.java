package com.ak.appliance.briko.comm.converter;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tec.uom.se.AbstractUnit;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BrikoVariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoVariable.class).stream().flatMap(v -> v.options().stream()))
        .isEqualTo(
            List.of(
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = BrikoVariable.class)
  void testFilterDelay(Variable<BrikoVariable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoVariable.class).stream().map(Variable::getUnit).collect(Collectors.toSet()))
        .isEqualTo(Set.of(AbstractUnit.ONE));
  }
}