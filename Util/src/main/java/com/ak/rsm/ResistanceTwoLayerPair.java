package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class ResistanceTwoLayerPair {
  @Nonnull
  private final TetrapolarSystemPair electrodeSystemPair;
  @Nonnegative
  private double rho1SI;
  @Nonnegative
  private double rho2SI;
  @Nonnegative
  private double hSI;
  private double dhSI;

  ResistanceTwoLayerPair(@Nonnull TetrapolarSystemPair electrodeSystemPair) {
    this.electrodeSystemPair = electrodeSystemPair;
  }

  ResistanceTwoLayerPair rho1(@Nonnegative double rho1SI) {
    this.rho1SI = rho1SI;
    return this;
  }

  ResistanceTwoLayerPair rho2(@Nonnegative double rho2SI) {
    this.rho2SI = rho2SI;
    return this;
  }

  ResistanceTwoLayerPair h(@Nonnegative double hSI) {
    this.hSI = hSI;
    return this;
  }

  ResistanceTwoLayerPair dh(double dhSI) {
    this.dhSI = dhSI;
    return this;
  }

  public double[] value() {
    return DoubleStream.of(hSI, hSI - dhSI).flatMap(h ->
        Arrays.stream(
            Arrays.stream(electrodeSystemPair.getPair()).mapToDouble(s ->
                new ResistanceTwoLayer(s).value(rho1SI, rho2SI, h)).toArray()
        )
    ).toArray();
  }

  public double[] value(@Nonnull double[] params) {
    if (params.length == 0 || params.length > 4) {
      throw new IllegalArgumentException(Arrays.toString(params));
    }
    for (int i = 0; i < params.length; i++) {
      if (i == 0) {
        rho1(params[i]);
      }
      else if (i == 1) {
        rho2(params[i]);
      }
      else if (i == 2) {
        h(params[i]);
      }
      else {
        dh(params[i]);
      }
    }
    return value();
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
