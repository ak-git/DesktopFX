package com.ak.comm.converter;

@FunctionalInterface
public interface Refreshable extends AutoCloseable {
  void refresh();

  @Override
  default void close() {
    //Empty implementation to remove Exception inherited from AutoCloseable.
  }
}
