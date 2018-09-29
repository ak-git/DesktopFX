package com.ak.digitalfilter;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface IntsAcceptor {
  IntsAcceptor EMPTY_INTS_ACCEPTOR = empty();

  void accept(@Nonnull int... values);

  static IntsAcceptor empty() {
    return values -> {
    };
  }
}
