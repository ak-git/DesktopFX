package com.ak.comm.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.IntBinaryOperator;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum OperatorVariables implements DependentVariable<TwoVariables, OperatorVariables> {
  OUT_PLUS(Integer::sum),
  OUT_MINUS((left, right) -> left - right) {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  OUT_DIV((left, right) -> left / right);

  private final IntBinaryOperator operator;

  OperatorVariables(IntBinaryOperator operator) {
    this.operator = operator;
  }


  @Override
  public final Class<TwoVariables> getInputVariablesClass() {
    return TwoVariables.class;
  }

  @Override
  public final List<TwoVariables> getInputVariables() {
    return Arrays.asList(TwoVariables.V1, TwoVariables.V2);
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().biOperator(() -> operator).build();
  }
}
