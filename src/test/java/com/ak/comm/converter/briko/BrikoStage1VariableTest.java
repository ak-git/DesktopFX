package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

import static com.ak.util.Strings.ANGLE;
import static org.assertj.core.api.Assertions.assertThat;

class BrikoStage1VariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(BrikoStage1Variable.class).stream().flatMap(v -> v.options().stream()))
        .allMatch(option -> option == Variable.Option.VISIBLE);
  }

  @ParameterizedTest
  @EnumSource(value = BrikoStage1Variable.class)
  void testFilterDelay(@Nonnull Variable<BrikoStage1Variable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(BrikoStage1Variable.class).stream().map(Variable::getUnit))
        .isEqualTo(
            List.of(
                AbstractUnit.ONE, AbstractUnit.ONE, AbstractUnit.ONE, AbstractUnit.ONE,
                Units.RADIAN.alternate(ANGLE).divide(1000.0), Units.RADIAN.alternate(ANGLE).divide(1000.0)
            )
        );
  }
}