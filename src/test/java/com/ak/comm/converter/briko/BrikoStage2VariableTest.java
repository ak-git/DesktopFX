package com.ak.comm.converter.briko;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

class BrikoStage2VariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoStage2Variable.class).stream().flatMap(v -> v.options().stream()).collect(Collectors.toSet()))
        .containsExactly(Variable.Option.VISIBLE);
  }

  @ParameterizedTest
  @EnumSource(value = BrikoStage2Variable.class)
  void testFilterDelay(@Nonnull Variable<BrikoStage2Variable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoStage2Variable.class).stream().map(Variable::getUnit))
        .isEqualTo(List.of(Units.GRAM, Units.GRAM, MILLI(METRE), AbstractUnit.ONE));
  }

  @ParameterizedTest
  @EnumSource(value = BrikoStage2Variable.class)
  void testInputVariablesClass(@Nonnull DependentVariable<BrikoStage1Variable, BrikoStage2Variable> variable) {
    assertThat(variable.getInputVariablesClass()).isEqualTo(BrikoStage1Variable.class);
  }
}