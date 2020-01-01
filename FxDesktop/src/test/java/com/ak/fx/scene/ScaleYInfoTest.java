package com.ak.fx.scene;

import java.util.Set;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class ScaleYInfoTest {
  private ScaleYInfoTest() {
  }

  @Test
  public void testToString() {
    Assert.assertEquals(new ScaleYInfo.ScaleYInfoBuilder<>(ADCVariable.ADC).mean(-3).scaleFactor(10).scaleFactor10(20).build().toString(),
        "ScaleYInfo{mean = -3, scaleFactor = 10, scaleFactor10 = 20}");
    Assert.assertEquals(new ScaleYInfo.ScaleYInfoBuilder<>(TestInverse.INVERSE).mean(-3).scaleFactor(10).scaleFactor10(20).build().toString(),
        "ScaleYInfo{mean = -3, scaleFactor = -10, scaleFactor10 = 20}");
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