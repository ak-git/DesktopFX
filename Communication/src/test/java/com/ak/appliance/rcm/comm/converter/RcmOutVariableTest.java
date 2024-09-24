package com.ak.appliance.rcm.comm.converter;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import java.util.stream.Stream;

import static com.ak.appliance.rcm.comm.converter.RcmOutVariable.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RcmOutVariableTest {
  @ParameterizedTest
  @EnumSource(RcmOutVariable.class)
  void testInputVariablesClass(DependentVariable<RcmInVariable, RcmOutVariable> variable) {
    assertThat(variable.getInputVariablesClass()).isEqualTo(RcmInVariable.class);
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"RHEO_1", "BASE_1", "RHEO_2", "BASE_2"}, mode = EnumSource.Mode.EXCLUDE)
  void testInputVariablesCount1(DependentVariable<RcmInVariable, RcmOutVariable> variable) {
    assertThat(variable.getInputVariables()).containsExactly(RcmInVariable.valueOf(variable.name()));
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"RHEO_1", "BASE_1", "RHEO_2", "BASE_2"})
  void testInputVariablesCount2(DependentVariable<RcmInVariable, RcmOutVariable> variable) {
    assertThat(variable.getInputVariables()).hasSize(2).contains(RcmInVariable.valueOf(variable.name()));
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"RHEO_1", "RHEO_2"})
  void testRheoChannels(Variable<RcmOutVariable> variable) {
    assertAll(variable.name(),
        () -> assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MICRO(Units.OHM)),
        () -> assertThat(variable.options()).contains(Variable.Option.INVERSE)
    );
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"BASE_1", "BASE_2"})
  void testBaseChannels(Variable<RcmOutVariable> variable) {
    assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MILLI(Units.OHM));
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"QS_1", "QS_2"})
  void testQoSChannels(Variable<RcmOutVariable> variable) {
    assertAll(variable.name(),
        () -> assertThat(variable.getUnit()).isEqualTo(Units.OHM),
        () -> assertThat(variable.options()).contains(Variable.Option.TEXT_VALUE_BANNER)
    );
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"RHEO_1", "RHEO_2", "ECG"})
  void testZeroInRange(Variable<RcmOutVariable> variable) {
    assertThat(variable.options()).contains(Variable.Option.FORCE_ZERO_IN_RANGE);
  }

  @ParameterizedTest
  @EnumSource(value = RcmOutVariable.class, names = {"ECG"})
  void testECGChannel(Variable<RcmOutVariable> variable) {
    assertThat(variable.getUnit()).isEqualTo(MetricPrefix.MILLI(Units.VOLT));
  }

  static Stream<Arguments> filterDelay() {
    return Stream.of(
        arguments(RHEO_1, 3.5),
        arguments(BASE_1, 377.0),
        arguments(QS_1, 3.5),
        arguments(ECG, 3.5),
        arguments(RHEO_2, 3.5),
        arguments(BASE_2, 377.0),
        arguments(QS_2, 3.5)
    );
  }

  @ParameterizedTest
  @MethodSource("filterDelay")
  void testFilterDelay(Variable<RcmOutVariable> variable, double delay) {
    assertThat(variable.filter().getDelay()).isEqualTo(delay);
  }
}