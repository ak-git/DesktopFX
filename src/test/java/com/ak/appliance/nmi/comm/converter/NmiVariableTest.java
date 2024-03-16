package com.ak.appliance.nmi.comm.converter;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.Units;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static tec.uom.se.unit.MetricPrefix.CENTI;

class NmiVariableTest {
  @Test
  void testOptions() {
    assertThat(EnumSet.allOf(NmiVariable.class).stream().flatMap(v -> v.options().stream()))
        .isEqualTo(
            List.of(
                Variable.Option.VISIBLE, Variable.Option.VISIBLE
            )
        );
  }

  @Test
  void testGetUnit() {
    assertThat(EnumSet.allOf(NmiVariable.class).stream().map(Variable::getUnit).collect(Collectors.toSet()))
        .isEqualTo(Set.of(AbstractUnit.ONE, CENTI(Units.OHM)));
  }
}