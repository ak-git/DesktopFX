package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PredictionTest {

  @Test
  public void testGetInequalityL2() {
    InexactTetrapolarSystem system1 = InexactTetrapolarSystem.milli(0.1).s(1.0).l(3.0);
    InexactTetrapolarSystem system2 = InexactTetrapolarSystem.milli(0.01).s(5.0).l(3.0);
    Measurement measurement1 = new TetrapolarMeasurement(system1, Resistance1Layer.layer1(1.0).applyAsDouble(system1));
    Measurement measurement2 = new TetrapolarMeasurement(system2, Resistance1Layer.layer1(1.0).applyAsDouble(system2));

    Assert.assertNotEquals(measurement1, measurement2);
    Assert.assertNotEquals(measurement1, new Object());
    Assert.assertNotEquals(new Object(), measurement2);
    Assert.assertNotEquals(measurement1.hashCode(), measurement2.hashCode());
    Assert.assertEquals(measurement1, measurement1);
    Assert.assertEquals(measurement2.merge(measurement1), measurement1.merge(measurement2));
    Assert.assertEquals(measurement1.merge(measurement2).hashCode(), measurement2.merge(measurement1).hashCode());

    Prediction prediction = new TetrapolarPrediction(measurement1.merge(measurement2), RelativeMediumLayers.SINGLE_LAYER, 10.0);
    Assert.assertEquals(prediction.getHorizons(), new double[] {Double.POSITIVE_INFINITY, 0.0}, prediction.toString());
    Assert.assertEquals(prediction.getResistivityPredicted(), 10.0, 0.001, prediction.toString());
    Assert.assertEquals(prediction.getInequalityL2(), 9.0 / 10.0, 0.001, prediction.toString());
  }

  @Test
  public void testGetInequalityL2Diff() {
    InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.1).s(1.0).l(2.0);

    Measurement measurementBefore = new TetrapolarMeasurement(system, new Resistance1Layer(system.toExact()).value(1.0));
    Measurement measurementAfter = new TetrapolarMeasurement(system, new Resistance1Layer(system.toExact()).value(2.0));
    DerivativeMeasurement measurement = new TetrapolarDerivativeMeasurement(measurementBefore, measurementAfter, Metrics.fromMilli(1.0));

    Assert.assertThrows(UnsupportedOperationException.class, () -> measurement.merge(measurement));

    Prediction prediction = new TetrapolarDerivativePrediction(measurement,
        new TetrapolarPrediction(measurement, RelativeMediumLayers.SINGLE_LAYER, 10.0), 1.0);
    Assert.assertEquals(prediction.getHorizons(), new double[] {Double.POSITIVE_INFINITY, 0.0}, prediction.toString());
    Assert.assertEquals(prediction.getResistivityPredicted(), 1.0, 0.001, prediction.toString());
    Assert.assertEquals(prediction.getInequalityL2(), 999.0, 0.001, prediction.toString());
  }
}