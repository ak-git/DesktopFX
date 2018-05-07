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
  private final double dhSI;

  ResistanceTwoLayerPair(@Nonnull TetrapolarSystemPair electrodeSystemPair, @Nonnegative double dhSI) {
    this.electrodeSystemPair = electrodeSystemPair;
    this.dhSI = dhSI;
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1SI specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2SI specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param hSI    height of <b>1-layer</b> in metres
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  public double[] value(@Nonnegative double rho1SI, @Nonnegative double rho2SI, @Nonnegative double hSI) {
    return DoubleStream.of(hSI, hSI - dhSI).flatMap(h ->
        Arrays.stream(
            Arrays.stream(electrodeSystemPair.getPair()).mapToDouble(s ->
                new ResistanceTwoLayer(s).value(rho1SI, rho2SI, h)).toArray()
        )
    ).toArray();
  }

  public double[] value(@Nonnull double[] rho1rho2h) {
    return value(rho1rho2h[0], rho1rho2h[1], rho1rho2h[2]);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
