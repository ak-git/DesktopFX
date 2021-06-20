package com.ak.math;

import java.util.StringJoiner;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import static com.ak.util.Strings.PLUS_MINUS;
import static com.ak.util.Strings.SPACE;

public final class ValuePair {
  private final double value;
  @Nonnegative
  private final double absError;

  public ValuePair(double value, @Nonnegative double absError) {
    this.value = value;
    this.absError = absError;
  }

  public ValuePair(double value) {
    this(value, 0.0);
  }

  public double getValue() {
    return value;
  }

  @Nonnegative
  public double getAbsError() {
    return absError;
  }

  @Override
  public String toString() {
    if (absError > 0) {
      int afterZero = (int) Math.abs(Math.min(Math.floor(StrictMath.log10(absError)), 0));
      return new StringJoiner(SPACE)
          .add("%%.%df".formatted(afterZero).formatted(value))
          .add(PLUS_MINUS).add("%%.%df".formatted(afterZero + 1).formatted(absError))
          .toString();
    }
    else {
      return Double.toString(value);
    }
  }

  public ValuePair mergeWith(@Nonnull ValuePair that) {
    RealVector x = new ArrayRealVector(new double[] {value});

    RealMatrix mA = new Array2DRowRealMatrix(new double[] {1.0});
    RealMatrix mH = new Array2DRowRealMatrix(new double[] {1.0});
    RealMatrix mQ = new Array2DRowRealMatrix(new double[] {0.0});
    RealMatrix mP0 = new Array2DRowRealMatrix(new double[] {absError});

    RealMatrix mR = new Array2DRowRealMatrix(new double[] {that.absError});
    ProcessModel pm = new DefaultProcessModel(mA, null, mQ, x, mP0);
    MeasurementModel mm = new DefaultMeasurementModel(mH, mR);
    var filter = new KalmanFilter(pm, mm);
    filter.predict();
    filter.correct(new ArrayRealVector(new double[] {that.value}));
    return new ValuePair(filter.getStateEstimation()[0], filter.getErrorCovariance()[0][0]);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    var valuePair = (ValuePair) o;
    return toString().equals(valuePair.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
