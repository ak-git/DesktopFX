package com.ak.fx.desktop.sktb;

import com.ak.comm.converter.sktbpr.SKTBVariable;
import com.ak.digitalfilter.IntsAcceptor;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

final class SKTBAngleVelocityControl implements IntsAcceptor {
  @Nonnull
  private final SKTBVariable variable;
  @Nonnull
  private final AtomicInteger angle = new AtomicInteger(0);
  @Nonnull
  private final AtomicInteger velocity = new AtomicInteger(0);

  SKTBAngleVelocityControl(@Nonnull SKTBVariable variable) {
    this.variable = variable;
  }

  @Override
  public void accept(@Nonnull int[] ints) {
    int error = angle.get() - ints[variable.ordinal()];
    velocity.set(error / 2);
  }

  @Nonnull
  SKTBVariable variable() {
    return variable;
  }

  void decrement() {
    angle.addAndGet(-10);
  }

  void increment() {
    angle.addAndGet(10);
  }

  void escape() {
    angle.set(0);
  }

  int velocity() {
    return velocity.get();
  }
}
