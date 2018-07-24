package com.ak.comm.converter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.IntBinaryOperator;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum OperatorVariables2 implements DependentVariable<OperatorVariables, OperatorVariables2> {
  OUT((left, right) -> left * right) {
    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  };

  private final IntBinaryOperator operator;

  OperatorVariables2(IntBinaryOperator operator) {
    this.operator = operator;
  }


  @Override
  public final Class<OperatorVariables> getInputVariablesClass() {
    return OperatorVariables.class;
  }

  @Override
  public final List<OperatorVariables> getInputVariables() {
    return Arrays.asList(OperatorVariables.OUT_PLUS, OperatorVariables.OUT_MINUS);
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().biOperator(() -> operator).build();
  }
}
