package com.ak.comm.converter;

public enum Variables {
  ;

  public static <E extends Enum<E> & Variable<E>> String toString(E variable, int value) {
    return String.format("%s = %d %s", variable.name(), value, variable.getUnit());
  }

  public static <E extends Enum<E> & Variable<E>> String toName(E variable) {
    return String.format("%s, %s", variable.name(), variable.getUnit());
  }
}
