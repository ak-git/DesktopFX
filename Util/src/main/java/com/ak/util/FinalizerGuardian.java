package com.ak.util;

public final class FinalizerGuardian {
  private final AutoCloseable finalizer;

  public FinalizerGuardian(AutoCloseable finalizer) {
    this.finalizer = finalizer;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      finalizer.close();
    }
    finally {
      super.finalize();
    }
  }
}
