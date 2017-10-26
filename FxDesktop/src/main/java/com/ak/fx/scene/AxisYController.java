package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;

public final class AxisYController<EV extends Enum<EV> & Variable<EV>> {
  private final List<EV> variables = new ArrayList<>();
  private final List<ScaleYInfo<EV>> scaleInfos = new ArrayList<>();
  @Nonnegative
  private int mmHeight = 1;
  @Nonnegative
  private int index = -1;

  public void setVariables(@Nonnull Collection<EV> variables) {
    this.variables.addAll(variables);
    scaleInfos.addAll(variables.stream().map(ev -> new ScaleYInfo.Builder<>(ev,
        scaleYInfo -> {
        }).build()).collect(Collectors.toList())
    );
  }

  public void setLineDiagramHeight(double lineDiagramHeight) {
    mmHeight = GridCell.mm(lineDiagramHeight);
  }

  public void scaleOrdered(@Nonnull int[] values, @Nonnull Consumer<ScaleYInfo<EV>> scaledConsumer) {
    index = (++index) % variables.size();

    IntSummaryStatistics intSummaryStatistics = IntStream.of(values).summaryStatistics();
    if (intSummaryStatistics.getMax() == intSummaryStatistics.getMin()) {
      intSummaryStatistics = IntStream.of(intSummaryStatistics.getMax(), 0).summaryStatistics();
    }

    int meanScaleFactor10 = scaleFactor10(mmHeight, intSummaryStatistics.getMax() - intSummaryStatistics.getMin()) * 10;
    int mean = (int) Math.rint((intSummaryStatistics.getMax() + intSummaryStatistics.getMin()) / 2.0 / meanScaleFactor10) * meanScaleFactor10;
    int signalRange = Math.max(Math.abs(intSummaryStatistics.getMax() - mean), Math.abs(intSummaryStatistics.getMin() - mean)) * 2;

    scaleInfos.set(index, new ScaleYInfo.Builder<>(variables.get(index), scaledConsumer).
        mean(mean).
        scaleFactor(optimizeScaleY(mmHeight, signalRange)).
        scaleFactor10(scaleFactor10(mmHeight, signalRange)).
        build());

    if (index == variables.size() - 1) {
      scaleInfos.forEach(ScaleYInfo::run);
    }
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
