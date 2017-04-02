package com.ak.comm.converter;

import java.util.function.IntBinaryOperator;
import java.util.stream.Stream;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum OperatorVariables2 implements DependentVariable<OperatorVariables> {
  OUT((left, right) -> left * right);

  private final IntBinaryOperator operator;

  OperatorVariables2(IntBinaryOperator operator) {
    this.operator = operator;
  }


  @Override
  public final Class<OperatorVariables> getInputVariablesClass() {
    return OperatorVariables.class;
  }

  @Override
  public final Stream<OperatorVariables> getInputVariables() {
    return Stream.of(OperatorVariables.OUT_PLUS, OperatorVariables.OUT_MINUS);
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().biOperator(() -> operator).build();
  }
}
