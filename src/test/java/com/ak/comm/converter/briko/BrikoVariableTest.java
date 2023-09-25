package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

import static com.ak.util.Strings.ANGLE;
import static org.assertj.core.api.Assertions.assertThat;

class BrikoVariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoVariable.class).stream().flatMap(v -> v.options().stream()))
        .isEqualTo(
            List.of(
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER,
                Variable.Option.VISIBLE, Variable.Option.TEXT_VALUE_BANNER
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = BrikoVariable.class)
  void testFilterDelay(@Nonnull Variable<BrikoVariable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoVariable.class).stream().map(Variable::getUnit))
        .isEqualTo(
            List.of(
                AbstractUnit.ONE, AbstractUnit.ONE, AbstractUnit.ONE, AbstractUnit.ONE,
                MetricPrefix.MILLI(Units.METRE), Units.RADIAN.alternate(ANGLE).divide(1000.0)
            )
        );
  }
}