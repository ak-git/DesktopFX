package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PredictionTest {

  @Test
  public void testGetInequalityL2() {
    InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.1).s(1.0).l(2.0);
    Measurement measurement = new TetrapolarMeasurement(system, new Resistance1Layer(system.toExact()).value(1.0));

    Prediction prediction = new TetrapolarPrediction(measurement, 10.0);
    Assert.assertEquals(prediction.getResistivityPredicted(), 10.0, 0.001, prediction.toString());
    Assert.assertEquals(prediction.getInequalityL2(), 9.0 / 10.0, 0.001, prediction.toString());
  }

  @Test
  public void testGetInequalityL2Diff() {
    InexactTetrapolarSystem system = InexactTetrapolarSystem.milli(0.1).s(1.0).l(2.0);

    Measurement measurementBefore = new TetrapolarMeasurement(system, new Resistance1Layer(system.toExact()).value(1.0));
    Measurement measurementAfter = new TetrapolarMeasurement(system, new Resistance1Layer(system.toExact()).value(2.0));
    DerivativeMeasurement measurement = new TetrapolarDerivativeMeasurement(measurementBefore, measurementAfter, Metrics.fromMilli(1.0));

    Prediction prediction = new TetrapolarDerivativePrediction(measurement, 10.0, 1.0);
    Assert.assertEquals(prediction.getResistivityPredicted(), 1.0, 0.001, prediction.toString());
    Assert.assertEquals(prediction.getInequalityL2(), 999.0, 0.001, prediction.toString());
  }
}