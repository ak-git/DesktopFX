package com.ak.digitalfilter;

interface IntsAcceptor {
  void accept(int... values);

  static IntsAcceptor empty() {
    return values -> {
    };
  }
}
