package com.ak.fx.scene;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variables;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AxisYControllerTest {
  @Test
  void testScaleToString() {
    assertThat(scale(IntStream.range(0, 1000))).extracting(ScaleYInfo::toString).asString()
        .startsWith("ScaleYInfo{mean = %d, scaleFactor = %d".formatted(500, 5));

    assertThat(scale(IntStream.range(0, 20))).extracting(ScaleYInfo::toString).asString()
        .startsWith("ScaleYInfo{mean = %d, scaleFactor = %d".formatted(10, 1));

    assertThat(scale(IntStream.generate(() -> 0).limit(10))).extracting(ScaleYInfo::toString).asString()
        .startsWith("ScaleYInfo{mean = %d, scaleFactor = %d".formatted(0, 1));

    assertThat(scale(IntStream.generate(() -> 1).limit(10))).extracting(ScaleYInfo::toString).asString()
        .startsWith("ScaleYInfo{mean = %d, scaleFactor = %d".formatted(0, 1));

    assertThat(scale(IntStream.generate(() -> 100).limit(10))).extracting(ScaleYInfo::toString).asString()
        .startsWith("ScaleYInfo{mean = %d, scaleFactor = %d".formatted(50, 1));
  }

  @Test
  void testScaleGridCell() {
    assertThat(Double.valueOf(GridCell.mm(scale(IntStream.range(0, 1000)).applyAsDouble(0))))
        .isEqualTo((0.0 - 500) / 5, Offset.offset(0.1));

    assertThat(Double.valueOf(GridCell.mm(scale(IntStream.range(0, 20)).applyAsDouble(0))))
        .isEqualTo(-10.0, Offset.offset(0.1));

    assertThat(Double.valueOf(GridCell.mm(scale(IntStream.generate(() -> 0).limit(10)).applyAsDouble(0))))
        .isEqualTo(0.0, Offset.offset(0.1));

    assertThat(Double.valueOf(GridCell.mm(scale(IntStream.generate(() -> 1).limit(10)).applyAsDouble(0))))
        .isEqualTo(0.0, Offset.offset(0.1));

    assertThat(Double.valueOf(GridCell.mm(scale(IntStream.generate(() -> 100).limit(10)).applyAsDouble(0))))
        .isEqualTo(-50.0, Offset.offset(0.1));
  }

  @Test
  void testScaleApply() {
    assertThat(scale(IntStream.range(0, 1000)).apply(0.0))
        .isEqualTo(Variables.toString(500, ADCVariable.ADC.getUnit(), 5));

    assertThat(scale(IntStream.range(0, 20)).apply(0.0))
        .isEqualTo(Variables.toString(10, ADCVariable.ADC.getUnit(), 1));

    assertThat(scale(IntStream.generate(() -> 0).limit(10)).apply(0.0))
        .isEqualTo(Variables.toString(0, ADCVariable.ADC.getUnit(), 10));

    assertThat(scale(IntStream.generate(() -> 1).limit(10)).apply(0.0))
        .isEqualTo(Variables.toString(0, ADCVariable.ADC.getUnit(), 10));

    assertThat(scale(IntStream.generate(() -> 100).limit(10)).apply(0.0))
        .isEqualTo(Variables.toString(50, ADCVariable.ADC.getUnit(), 1));
  }

  private static ScaleYInfo<ADCVariable> scale(IntStream data) {
    AxisYController<ADCVariable> controller = new AxisYController<>();
    controller.setLineDiagramHeight(GridCell.mmToScreen(233));
    return controller.scale(ADCVariable.ADC, data.toArray());
  }
}