package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import tec.uom.se.AbstractUnit;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.GRAM;
import static tec.uom.se.unit.Units.METRE;

class BrikoStage1VariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoStage1Variable.class).stream().flatMap(v -> v.options().stream()))
        .allMatch(option -> option == Variable.Option.VISIBLE);
  }

  @Test
  void testFilterDelay() {
    assertThat(EnumSet.allOf(BrikoStage1Variable.class).stream().mapToDouble(value -> value.filter().getDelay()).toArray())
        .containsExactly(34.0, 34.0, 34.0, 34.0, 0.0, 0.0);
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoStage1Variable.class).stream().map(Variable::getUnit))
        .isEqualTo(
            List.of(
                GRAM, GRAM, GRAM, GRAM, MILLI(METRE), AbstractUnit.ONE
            )
        );
  }
}