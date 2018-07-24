package com.ak.fx.scene;

import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;

public final class AxisYController<EV extends Enum<EV> & Variable<EV>> {
  @Nonnegative
  private int mmHeight = 1;

  public void setLineDiagramHeight(@Nonnegative double lineDiagramHeight) {
    mmHeight = GridCell.mm(lineDiagramHeight);
  }

  public ScaleYInfo<EV> scale(@Nonnull EV variable, @Nonnull int[] values) {
    IntSummaryStatistics intSummaryStatistics = IntStream.of(values).summaryStatistics();
    if (intSummaryStatistics.getMax() == intSummaryStatistics.getMin()) {
      intSummaryStatistics = IntStream.of(intSummaryStatistics.getMax(), 0).summaryStatistics();
    }

    int meanScaleFactor10 = scaleFactor10(mmHeight, intSummaryStatistics.getMax() - intSummaryStatistics.getMin()) * 10;
    int mean = (int) Math.rint((intSummaryStatistics.getMax() + intSummaryStatistics.getMin()) / 2.0 / meanScaleFactor10) * meanScaleFactor10;
    int signalRange = Math.max(Math.abs(intSummaryStatistics.getMax() - mean), Math.abs(intSummaryStatistics.getMin() - mean)) * 2;
    return new ScaleYInfo.Builder<>(variable).mean(mean).
        scaleFactor(optimizeScaleY(mmHeight, signalRange)).
        scaleFactor10(scaleFactor10(mmHeight, signalRange)).
        build();
  }

  private static int scaleFactor10(@Nonnegative int range, @Nonnegative int signalRange) {
    return Math.max(1, (int) StrictMath.pow(10.0, Math.ceil(Math.max(0, StrictMath.log10(signalRange * 1.0 / range)))));
  }

  private static int optimizeScaleY(@Nonnegative int range, @Nonnegative int signalRange) {
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
