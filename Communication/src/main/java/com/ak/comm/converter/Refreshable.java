package com.ak.comm.converter;

@FunctionalInterface
public interface Refreshable {
  void refresh(boolean force);

  default void close() {
    //Empty implementation to remove Exception inherited from AutoCloseable.
  }
}
