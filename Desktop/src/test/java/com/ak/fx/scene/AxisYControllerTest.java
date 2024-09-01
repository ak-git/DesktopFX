package com.ak.fx.scene;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variables;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnegative;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AxisYControllerTest {
  @Test
  void testScale() {
    testScale(IntStream.range(0, 1000), 500, 5);
    testScale(IntStream.range(0, 20), 10, 1);
    testScale(IntStream.generate(() -> 0).limit(10), 0, 1);
    testScale(IntStream.generate(() -> 1).limit(10), 0, 1);
    testScale(IntStream.generate(() -> 100).limit(10), 50, 1);
  }

  private static void testScale(IntStream data, int mean, @Nonnegative int scaleFactor) {
    AxisYController<ADCVariable> controller = new AxisYController<>();
    controller.setLineDiagramHeight(GridCell.mmToScreen(233));
    ScaleYInfo<ADCVariable> scaleYInfo = controller.scale(ADCVariable.ADC, data.toArray());
    assertThat(scaleYInfo.toString()).startsWith("ScaleYInfo{mean = %d, scaleFactor = %d".formatted(mean, scaleFactor));
    assertThat(Double.valueOf(GridCell.mm(scaleYInfo.applyAsDouble(0))))
        .isEqualTo((0.0 - mean) / scaleFactor, Offset.offset(0.1));
    assertThat(scaleYInfo.apply(0.0)).isEqualTo(Variables.toString(mean, ADCVariable.ADC.getUnit(), scaleFactor));
  }
}