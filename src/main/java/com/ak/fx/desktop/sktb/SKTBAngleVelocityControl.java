package com.ak.fx.desktop.sktb;

import com.ak.comm.converter.rsce.RsceVariable;
import com.ak.comm.converter.sktbpr.SKTBVariable;
import com.ak.digitalfilter.IntsAcceptor;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

final class SKTBAngleVelocityControl implements IntsAcceptor {
  @Nonnull
  private final SKTBVariable variable;
  @Nonnull
  private final RsceVariable rsceMapping;
  @Nonnull
  private final AtomicInteger angle = new AtomicInteger(0);
  @Nonnull
  private final AtomicInteger velocity = new AtomicInteger(0);

  SKTBAngleVelocityControl(@Nonnull SKTBVariable variable) {
    this.variable = variable;
    rsceMapping = switch (variable) {
      case ROTATE -> RsceVariable.ROTATE;
      case FLEX -> RsceVariable.OPEN;
    };
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

  @Nonnull
  RsceVariable rsceMapping() {
    return rsceMapping;
  }

  void decrement() {
    angle.addAndGet(-10);
  }

  void increment() {
    angle.addAndGet(10);
  }

  boolean isUpdatedBy(int percents) {
    int newValue = 180 * percents / 100 - 90;
    return angle.getAndSet(newValue) != newValue;
  }

  void escape() {
    angle.set(0);
  }

  int velocity() {
    return velocity.get();
  }
}
