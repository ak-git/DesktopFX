package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.ak.comm.converter.Variable.Option.TEXT_VALUE_BANNER;
import static com.ak.comm.converter.Variable.Option.VISIBLE;
import static org.assertj.core.api.Assertions.assertThat;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.PASCAL;

class BrikoStage2VariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoStage2Variable.class).stream().flatMap(v -> v.options().stream()).collect(Collectors.toSet()))
        .contains(VISIBLE, TEXT_VALUE_BANNER);
  }

  @ParameterizedTest
  @EnumSource(value = BrikoStage2Variable.class)
  void testFilterDelay(@Nonnull Variable<BrikoStage2Variable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoStage2Variable.class).stream().map(Variable::getUnit))
        .isEqualTo(List.of(Units.GRAM, Units.GRAM, MILLI(METRE), PASCAL));
  }
}