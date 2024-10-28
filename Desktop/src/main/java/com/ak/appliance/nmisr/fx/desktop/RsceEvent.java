package com.ak.appliance.nmisr.fx.desktop;

import com.ak.appliance.rsce.comm.converter.RsceVariable;
import com.ak.comm.converter.Variables;
import org.springframework.context.ApplicationEvent;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public final class RsceEvent extends ApplicationEvent {
  private final int[] values;

  public RsceEvent(Object source, int[] values) {
    super(source);
    this.values = Arrays.copyOf(values, values.length);
  }

  public int getValue(RsceVariable variable) {
    return values[variable.ordinal()];
  }

  @Override
  public String toString() {
    return "RsceEvent{values = {%s}, source = %s}".formatted(
        EnumSet.allOf(RsceVariable.class).stream()
            .map(v -> Variables.toString(v, values[v.ordinal()])).collect(Collectors.joining("; ")),
        source);
  }
}
