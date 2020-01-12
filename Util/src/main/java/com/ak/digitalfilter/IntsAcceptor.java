package com.ak.digitalfilter;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface IntsAcceptor {
  void accept(@Nonnull int... values);
}
