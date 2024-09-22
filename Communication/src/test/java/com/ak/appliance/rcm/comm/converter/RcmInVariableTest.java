package com.ak.appliance.rcm.comm.converter;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class RcmInVariableTest {
  @ParameterizedTest
  @EnumSource(value = RcmInVariable.class, names = {"RHEO_1X", "RHEO_2X", "ECG_X"})
  void testVarsNoOptions(Variable<RcmInVariable> variable) {
    assertThat(variable.options()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = RcmInVariable.class, names = {"RHEO_1X", "RHEO_2X", "ECG_X"}, mode = EnumSource.Mode.EXCLUDE)
  void testVarsWithOptions(Variable<RcmInVariable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
  }

  @ParameterizedTest
  @EnumSource(RcmInVariable.class)
  void testFilterDelay(Variable<RcmInVariable> variable) {
    assertThat(variable.filter().getDelay()).isZero();
  }

  @ParameterizedTest
  @EnumSource(value = RcmInVariable.class, names = {"RHEO_1", "RHEO_2", "ECG"})
  void testFilterSigned(Variable<RcmInVariable> variable) {
    DigitalFilter filter = variable.filter();
    filter.forEach(values -> assertThat(values).containsExactly(-2048));
    filter.accept(2 << 10);
  }

  @ParameterizedTest
  @EnumSource(value = RcmInVariable.class, names = {"RHEO_1", "RHEO_2", "ECG"}, mode = EnumSource.Mode.EXCLUDE)
  void testFilterNonSigned(Variable<RcmInVariable> variable) {
    DigitalFilter filter = variable.filter();
    filter.forEach(values -> assertThat(values).containsExactly(2048));
    filter.accept(2 << 10);
  }

  @ParameterizedTest
  @EnumSource(RcmInVariable.class)
  void testFilterAll(Variable<RcmInVariable> variable) {
    DigitalFilter filter = variable.filter();
    filter.forEach(values -> assertThat(values).containsExactly(2047));
    filter.accept((2 << 10) - 1);
  }
}