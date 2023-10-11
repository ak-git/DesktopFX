package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import tec.uom.se.unit.MetricPrefix;

import java.util.EnumSet;
import java.util.List;

import static com.ak.comm.converter.Variable.Option.TEXT_VALUE_BANNER;
import static com.ak.comm.converter.Variable.Option.VISIBLE;
import static com.ak.util.Strings.ANGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.RADIAN;

class BrikoStage2EncoderVariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoStage2EncoderVariable.class).stream().flatMap(v -> v.options().stream()))
        .isEqualTo(
            List.of(VISIBLE, TEXT_VALUE_BANNER)
        );
  }

  @Test
  void testFilterDelay() {
    assertThat(EnumSet.allOf(BrikoStage2EncoderVariable.class).stream().mapToDouble(value -> value.filter().getDelay()).toArray())
        .containsExactly(34.0, 0.0);
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoStage2EncoderVariable.class).stream().map(Variable::getUnit))
        .isEqualTo(List.of(RADIAN.alternate(ANGLE).divide(1000.0), MetricPrefix.MILLI(METRE)));
  }
}