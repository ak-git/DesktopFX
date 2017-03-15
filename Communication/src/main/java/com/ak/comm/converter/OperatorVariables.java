package com.ak.comm.converter;

import java.util.function.IntBinaryOperator;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum OperatorVariables implements Variable {
  OUT_PLUS((left, right) -> left + right),
  OUT_MINUS((left, right) -> left - right);

  private final IntBinaryOperator operator;

  OperatorVariables(IntBinaryOperator operator) {
    this.operator = operator;
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().function(value -> operator.applyAsInt(value[0], value[1])).build();
  }
}
