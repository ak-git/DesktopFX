package com.ak.fx.scene;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScaleYInfoTest {
  @ParameterizedTest
  @EnumSource
  void testADC(ADCVariable v) {
    assertThat(new ScaleYInfo.ScaleYInfoBuilder<>(v).mean(-3).scaleFactor(10).scaleFactor10(20).build())
        .hasToString("ScaleYInfo{mean = -3, scaleFactor = 10, scaleFactor10 = 20}");
  }

  @ParameterizedTest
  @EnumSource
  void testInverse(TestInverse v) {
    assertThat(new ScaleYInfo.ScaleYInfoBuilder<>(v).mean(-3).scaleFactor(10).scaleFactor10(20).build())
        .hasToString("ScaleYInfo{mean = -3, scaleFactor = -10, scaleFactor10 = 20}");
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