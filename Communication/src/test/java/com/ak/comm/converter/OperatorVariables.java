package com.ak.comm.converter;

import java.util.function.IntBinaryOperator;
import java.util.stream.Stream;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum OperatorVariables implements DependentVariable<TwoVariables> {
  OUT_PLUS((left, right) -> left + right),
  OUT_MINUS((left, right) -> left - right);

  private final IntBinaryOperator operator;

  OperatorVariables(IntBinaryOperator operator) {
    this.operator = operator;
  }


  @Override
  public final Class<TwoVariables> getInputVariablesClass() {
    return TwoVariables.class;
  }

  @Override
  public final Stream<TwoVariables> getInputVariables() {
    return Stream.of(TwoVariables.V1, TwoVariables.V2);
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().biOperator(operator).build();
  }
}
