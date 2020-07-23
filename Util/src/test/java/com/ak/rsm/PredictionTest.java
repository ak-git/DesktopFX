package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.unit.Units;

public class PredictionTest {

  @Test
  public void testGetInequalityL2() {
    TetrapolarSystem system = new TetrapolarSystem(1, 2, Units.METRE);
    Measurement measurement = new TetrapolarMeasurement(system, new Resistance1Layer(system).value(1.0));

    Prediction prediction = new TetrapolarPrediction(measurement, 10.0);
    Assert.assertEquals(prediction.getInequalityL2(), new double[] {9.0}, 0.001, prediction.toString());
  }

  @Test
  public void testGetInequalityL2Diff() {
    TetrapolarSystem system = new TetrapolarSystem(1, 2, Units.METRE);

    Measurement measurementBefore = new TetrapolarMeasurement(system, new Resistance1Layer(system).value(1.0));
    Measurement measurementAfter = new TetrapolarMeasurement(system, new Resistance1Layer(system).value(2.0));
    DerivativeMeasurement measurement = new TetrapolarDerivativeMeasurement(measurementBefore, measurementAfter, Metrics.fromMilli(1.0));

    Prediction prediction = new TetrapolarDerivativePrediction(measurement, 10.0, 1.0);
    Assert.assertEquals(prediction.getInequalityL2(), new double[] {9.0, 999.0}, 0.001, prediction.toString());
  }
}