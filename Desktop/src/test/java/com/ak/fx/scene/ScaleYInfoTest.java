package com.ak.fx.scene;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ScaleYInfoTest {
  @Test
  void testADC() {
    Stream.of(ADCVariable.values()).forEach(v ->
        assertThat(new ScaleYInfo.ScaleYInfoBuilder<>(v).mean(-3).scaleFactor(10).scaleFactor10(20).build())
            .hasToString("ScaleYInfo{mean = -3, scaleFactor = 10, scaleFactor10 = 20}")
    );
  }

  @Test
  void testInverse() {
    Stream.of(TestInverse.values()).forEach(testInverse ->
        assertThat(new ScaleYInfo.ScaleYInfoBuilder<>(testInverse).mean(-3).scaleFactor(10).scaleFactor10(20).build())
            .hasToString("ScaleYInfo{mean = -3, scaleFactor = -10, scaleFactor10 = 20}")
    );
  }

  private enum TestInverse implements Variable<TestInverse> {
    INVERSE {
      @Override
      public Set<Option> options() {
        return Option.addToDefault(Option.INVERSE);
      }
    }
  }
}