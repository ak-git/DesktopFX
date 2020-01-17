package com.ak.math;

import java.util.logging.Logger;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.testng.annotations.Test;

import static java.lang.StrictMath.pow;

public class KalmanFilterTest {
  @Test
  public void test() {
    double dt = 1.0;
    double measurementNoise = 5.0;
    double velocityNoise2 = 0.1;

    RealMatrix A = new Array2DRowRealMatrix(new double[][] {
        {1},
    });
    RealMatrix B = new Array2DRowRealMatrix(new double[][] {
        {dt}
    });
    RealMatrix H = new Array2DRowRealMatrix(new double[][] {
        {1.0}
    });
    RealVector x = new ArrayRealVector(new double[] {0});

    RealMatrix Q = new Array2DRowRealMatrix(new double[][] {
        {velocityNoise2 * dt * dt}
    });
    RealMatrix P = new Array2DRowRealMatrix(new double[][] {
        {1}
    });
    RealMatrix R = new Array2DRowRealMatrix(new double[] {
        pow(measurementNoise, 2.0)
    });

    ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P);
    MeasurementModel mm = new DefaultMeasurementModel(H, R);
    KalmanFilter filter = new KalmanFilter(pm, mm);

    RandomGenerator rand = new JDKRandomGenerator();

    RealVector mNoise = new ArrayRealVector(1);
    for (int i = 0; i < 1000; i++) {
      RealVector u = new ArrayRealVector(new double[] {(0.5 * i / 1000) * StrictMath.sin(2.0 * Math.PI * 1.0 / 200.0 * i)});
      filter.predict(u);

      RealVector pNoise = new ArrayRealVector(new double[] {velocityNoise2 * rand.nextGaussian() * pow(dt, 2.0)});
      // x = A * x + B * u + pNoise
      x = A.operate(x).add(B.operate(u)).add(pNoise);
      // simulate the measurement
      mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
      // z = H * x + m_noise
      RealVector z = H.operate(x).add(mNoise);
      filter.correct(z);

      double position = filter.getStateEstimation()[0];
      String format = String.format("%.6f\t%.3f\t%.3f\t%.3f", u.getEntry(0), x.getEntry(0), z.getEntry(0), position);
      Logger.getAnonymousLogger().info(format);
    }
  }

  @Test
  public void testConstantVoltage() {
    double constantVoltage = 10.0;
    double measurementNoise = 0.1;
    double processNoise = 1.0e-5;

    RealMatrix A = new Array2DRowRealMatrix(new double[] {1.0});
    RealMatrix B = null;
    RealMatrix H = new Array2DRowRealMatrix(new double[] {1.0});
    RealVector x = new ArrayRealVector(new double[] {constantVoltage});
    RealMatrix Q = new Array2DRowRealMatrix(new double[] {processNoise});
    RealMatrix P = new Array2DRowRealMatrix(new double[] {1.0});
    RealMatrix R = new Array2DRowRealMatrix(new double[] {measurementNoise});

    ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P);
    MeasurementModel mm = new DefaultMeasurementModel(H, R);
    KalmanFilter filter = new KalmanFilter(pm, mm);

    RealVector pNoise = new ArrayRealVector(1);
    RealVector mNoise = new ArrayRealVector(1);

    RandomGenerator rand = new JDKRandomGenerator();
    for (int i = 0; i < 100; i++) {
      filter.predict();
      // simulate the process
      pNoise.setEntry(0, processNoise * rand.nextGaussian());
      // x = A * x + p_noise
      x = A.operate(x);
      // simulate the measurement
      mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
      // z = H * x + m_noise
      RealVector z = H.operate(x).add(mNoise);
      filter.correct(z);
      double voltage = filter.getStateEstimation()[0];
      Logger.getAnonymousLogger().info(String.format("%.3f%n", voltage));
    }
  }
}
