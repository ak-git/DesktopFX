package com.ak.fx.scene;

import com.ak.comm.converter.Variable;
import com.ak.util.Numbers;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.stream.IntStream;

public final class AxisYController<V extends Enum<V> & Variable<V>> {
  private int mmHeight = 1;

  public void setLineDiagramHeight(double lineDiagramHeight) {
    mmHeight = GridCell.mm(lineDiagramHeight);
  }

  public ScaleYInfo<V> scale(V variable, int[] values) {
    DescriptiveStatistics stats = new DescriptiveStatistics();
    IntStream.of(values).forEach(stats::addValue);
    int max = Numbers.toInt(stats.getPercentile(98));
    int min = Numbers.toInt(stats.getPercentile(2));

    int peakToPeak = max - min;
    if (peakToPeak == 0) {
      min = 0;
    }

    int meanScaleFactor10 = scaleFactor10(mmHeight, peakToPeak) * 10;
    var mean = 0;
    if (!variable.options().contains(Variable.Option.FORCE_ZERO_IN_RANGE)) {
      mean = Numbers.toInt((max + min) / 2.0 / meanScaleFactor10) * meanScaleFactor10;
    }
    int signalRange = Math.max(Math.abs(max - mean), Math.abs(min - mean)) * 2;
    return new ScaleYInfo.ScaleYInfoBuilder<>(variable).mean(mean).
        scaleFactor(optimizeScaleY(mmHeight, signalRange)).
        scaleFactor10(scaleFactor10(mmHeight, signalRange)).
        build();
  }

  private static int scaleFactor10(int range, int signalRange) {
    return Math.max(1, (int) StrictMath.pow(10.0, Math.ceil(Math.max(0, StrictMath.log10(signalRange * 1.0 / range)))));
  }

  private static int optimizeScaleY(int range, int signalRange) {
    int scaleFactor10 = scaleFactor10(range, signalRange);
    int scaleFactor = scaleFactor10;
    int scaledRange = Math.max(1, signalRange / scaleFactor10);
    if (range / scaledRange >= 5) {
      scaleFactor = scaleFactor10 / 5;
    }
    else if (range / scaledRange >= 2) {
      scaleFactor = scaleFactor10 / 2;
    }
    return Math.max(1, scaleFactor);
  }
}
