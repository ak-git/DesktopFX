package com.ak.fx.scene;

import java.util.stream.IntStream;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variables;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AxisYControllerTest {
  private AxisYControllerTest() {
  }

  @DataProvider(name = "fullData")
  public static Object[][] fullData() {
    return new Object[][] {
        {IntStream.range(0, 1000), 500, 5},
        {IntStream.range(0, 20), 10, 1},
        {IntStream.generate(() -> 1).limit(10), 0, 1},
        {IntStream.generate(() -> 100).limit(10), 50, 1},
    };
  }


  @Test(dataProvider = "fullData")
  public static void testScale(IntStream data, int mean, int scaleFactor) {
    AxisYController<ADCVariable> controller = new AxisYController<>();
    controller.setLineDiagramHeight(1000);
    ScaleYInfo<ADCVariable> scaleYInfo = controller.scale(ADCVariable.ADC, data.toArray());
    Assert.assertTrue(scaleYInfo.toString().startsWith(String.format("ScaleYInfo{mean = %d, scaleFactor = %d", mean, scaleFactor)), scaleYInfo.toString());
    Assert.assertEquals(GridCell.mm(scaleYInfo.applyAsDouble(0)), (0.0 - mean) / scaleFactor, 0.1, scaleYInfo.toString());
    Assert.assertEquals(scaleYInfo.apply(0.0), Variables.toString(mean, ADCVariable.ADC.getUnit(), scaleFactor), scaleYInfo.toString());
  }
}