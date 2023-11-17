package com.ak.fx.desktop.nmisr;

import com.ak.comm.converter.Variables;
import com.ak.comm.converter.rsce.RsceVariable;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  @Override
  public String toString() {
    return "RsceEvent{values = {%s}, source = %s}".formatted(
        Stream.of(RsceVariable.values()).map(v -> Variables.toString(v, values[v.ordinal()])).collect(Collectors.joining(", ")),
        source);
  }
}
