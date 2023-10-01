package com.ak.fx.desktop.nmisr;

import com.ak.comm.converter.rsce.RsceVariable;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;

public final class RsceEvent extends ApplicationEvent {

  @Nonnull
  private final int[] values;

  public RsceEvent(@Nonnull Object source, @Nonnull int[] values) {
    super(source);
    this.values = Arrays.copyOf(values, values.length);
  }

  public int getValue(@Nonnull RsceVariable variable) {
    return values[variable.ordinal()];
  }
}
