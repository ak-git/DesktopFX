package com.ak.appliance.prv.comm.converter;

import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tech.units.indriya.AbstractUnit;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PrvVariableTest {
  @Test
  void options() {
    assertThat(EnumSet.allOf(PrvVariable.class)).extracting(Variable::options).containsOnly(Set.of(Variable.Option.VISIBLE));
  }

  @ParameterizedTest
  @EnumSource(value = PrvVariable.class, names = "ADC_SMOOTH", mode = EnumSource.Mode.EXCLUDE)
  void filterDelayZero(Variable<PrvVariable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @ParameterizedTest
  @EnumSource(value = PrvVariable.class, names = "ADC_SMOOTH")
  void filterDelay(Variable<PrvVariable> variable) {
    assertThat(variable.filter().getDelay()).isCloseTo(15.5, within(0.01));
  }

  @Test
  void getUnit() {
    assertThat(EnumSet.allOf(PrvVariable.class).stream().map(Variable::getUnit).collect(Collectors.toSet()))
        .isEqualTo(Set.of(AbstractUnit.ONE));
  }
}