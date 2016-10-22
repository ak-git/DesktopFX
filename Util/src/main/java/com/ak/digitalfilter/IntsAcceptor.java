package com.ak.digitalfilter;

interface IntsAcceptor {
  IntsAcceptor EMPTY_INTS_ACCEPTOR = empty();

  void accept(int... values);

  static IntsAcceptor empty() {
    return values -> {
    };
  }
}
