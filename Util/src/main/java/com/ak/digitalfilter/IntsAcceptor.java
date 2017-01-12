package com.ak.digitalfilter;

public interface IntsAcceptor {
  IntsAcceptor EMPTY_INTS_ACCEPTOR = empty();

  void accept(int... values);

  static IntsAcceptor empty() {
    return values -> {
    };
  }
}
