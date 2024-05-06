package com.ak.digitalfilter;

@FunctionalInterface
public interface IntsAcceptor {
  int[] EMPTY_INTS = {};

  void accept(int... values);
}
