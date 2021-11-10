package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PredictionTest {

  @Test
  public void testGetInequalityL2() {
    TetrapolarSystem system1 = TetrapolarSystem.milli(0.1).s(1.0).l(3.0);
    TetrapolarSystem system2 = TetrapolarSystem.milli(0.01).s(5.0).l(3.0);
    Measurement measurement1 = new TetrapolarMeasurement(system1, 1.0);
    Measurement measurement2 = new TetrapolarMeasurement(system2, 1.0);

    Assert.assertNotEquals(measurement1, measurement2);
    Assert.assertNotEquals(measurement1, new Object());
    Assert.assertNotEquals(new Object(), measurement2);
    Assert.assertNotEquals(measurement1.hashCode(), measurement2.hashCode());
    Assert.assertEquals(measurement1, measurement1);

    Measurement merge1 = measurement1.merge(measurement2);
    Measurement merge2 = measurement2.merge(measurement1);
    Assert.assertEquals(merge1.system().getS(), merge2.system().getS(), 1.0e-6);
    Assert.assertEquals(merge1.system().getL(), merge2.system().getL(), 1.0e-6);
    Assert.assertEquals(merge1.resistivity(), merge2.resistivity(), 1.0e-6);

    Prediction prediction = new TetrapolarPrediction(
        merge1.system(), RelativeMediumLayers.SINGLE_LAYER, 10.0, merge1.resistivity());
    Assert.assertEquals(prediction.getHorizons(), new double[] {Double.POSITIVE_INFINITY, 0.0}, prediction.toString());
    Assert.assertEquals(prediction.getResistivityPredicted(), 10.0, 0.001, prediction.toString());
  }

  @Test
  public void testGetInequalityL2Diff() {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(1.0).l(2.0);

    Measurement measurementBefore = new TetrapolarMeasurement(system, 1.0);
    Measurement measurementAfter = new TetrapolarMeasurement(system, 2.0);
    DerivativeMeasurement measurement = new TetrapolarDerivativeMeasurement(measurementBefore, measurementAfter, Metrics.fromMilli(1.0));

    Assert.assertThrows(UnsupportedOperationException.class, () -> measurement.merge(measurement));

    Prediction prediction = new TetrapolarDerivativePrediction(measurement.system(), RelativeMediumLayers.SINGLE_LAYER, 10.0,
        new double[] {measurement.resistivity(), measurement.derivativeResistivity()});
    Assert.assertEquals(prediction.getHorizons(), new double[] {Double.POSITIVE_INFINITY, 0.0}, prediction.toString());
    Assert.assertEquals(prediction.getResistivityPredicted(), Double.NaN, 0.001, prediction.toString());
  }
}