package com.ak.appliance.nmisr.fx.desktop;

import com.ak.appliance.rsce.comm.converter.RsceVariable;
import com.ak.comm.converter.Variables;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.random.RandomGenerator;

class RsceEventTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @Test
  void testGetValue() {
    int[] inputValues = RANDOM.ints(RsceVariable.values().length).toArray();
    RsceEvent event = new RsceEvent(this, inputValues);
    Assertions.assertThat(EnumSet.allOf(RsceVariable.class).stream().mapToInt(event::getValue).toArray())
        .as(event::toString)
        .containsExactly(inputValues);
  }

  @Test
  void testToString() {
    int[] inputValues = RANDOM.ints(RsceVariable.values().length).toArray();
    RsceEvent event = new RsceEvent(this, inputValues);
    EnumSet.allOf(RsceVariable.class).stream().map(v -> Variables.toString(v, inputValues[v.ordinal()]))
        .forEach(s -> Assertions.assertThat(event.toString()).contains(s));
  }
}