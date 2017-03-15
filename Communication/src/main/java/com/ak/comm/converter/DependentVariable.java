package com.ak.comm.converter;

import java.util.stream.Stream;

public interface DependentVariable<IN extends Enum<IN> & Variable> extends Variable {
  Stream<IN> getInputVariables();
}
