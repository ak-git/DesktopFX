package com.ak.appliance.sktbpr.fx.desktop;

import com.ak.appliance.nmisr.fx.desktop.RsceEvent;
import com.ak.appliance.rsce.comm.converter.RsceVariable;
import com.ak.appliance.sktbpr.comm.converter.SKTBVariable;
import com.ak.digitalfilter.IntsAcceptor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

final class SKTBAngleVelocityControl implements IntsAcceptor {
  private final SKTBVariable variable;
  private final RsceVariable rsceMapping;
  private final AtomicInteger angle = new AtomicInteger(0);
  private final AtomicInteger velocity = new AtomicInteger(0);

  SKTBAngleVelocityControl(SKTBVariable variable) {
    this.variable = Objects.requireNonNull(variable);
    rsceMapping = switch (variable) {
      case ROTATE -> RsceVariable.ROTATE;
      case FLEX -> RsceVariable.OPEN;
    };
  }

  @Override
  public void accept(int[] ints) {
    int error = angle.get() - ints[variable.ordinal()];
    velocity.set((error / 2) * 1000);
  }

  void decrement() {
    angle.addAndGet(-10);
  }

  void increment() {
    angle.addAndGet(10);
  }

  void update(RsceEvent event) {
    int percents = event.getValue(rsceMapping);
    angle.set(-(180 * percents / 100 - 90));
  }

  void escape() {
    angle.set(0);
  }

  int velocity() {
    return velocity.get();
  }
}
