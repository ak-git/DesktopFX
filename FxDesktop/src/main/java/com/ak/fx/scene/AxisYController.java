package com.ak.fx.scene;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

final class AxisYController<EV extends Enum<EV> & Variable<EV>> {
  private final Map<EV, DigitalFilter> stdFilters = new LinkedHashMap<>();
  private final Map<EV, int[]> meanAndStdValues = new LinkedHashMap<>();
  @Nonnegative
  private int mmHeight = 1;
  @Nonnegative
  private int maxSamples = 1;

  void setLineDiagramHeight(@Nonnegative double lineDiagramHeight) {
    mmHeight = GridCell.mm(lineDiagramHeight);
  }

  void setMaxSamples(@Nonnegative int maxSamples) {
    int prev = this.maxSamples;
    this.maxSamples = Math.max(2, maxSamples);
    if (prev != this.maxSamples) {
      stdFilters.replaceAll((ev, digitalFilter) -> null);
    }
  }

  public void setVariables(@Nonnull Iterable<EV> variables) {
    for (EV variable : variables) {
      stdFilters.put(variable, null);
    }
  }

  ScaleYInfo<EV> scale(@Nonnull EV variable, @Nonnull int[] values) {
    DigitalFilter stdFilter = stdFilters.get(variable);
    if (stdFilter == null) {
      stdFilter = FilterBuilder.of().recursiveMeanAndStd(maxSamples).build();
      stdFilters.put(variable, stdFilter);
      meanAndStdValues.put(variable, new int[] {0, 0});
      stdFilter.forEach(ints -> meanAndStdValues.put(variable, ints));
    }
    for (int value : values) {
      stdFilter.accept(value);
    }

    int mean = 0;
    int signalRange = (int) (3.5 * meanAndStdValues.get(variable)[1]);
    if (!variable.options().contains(Variable.Option.FORCE_ZERO_IN_RANGE)) {
      int meanScaleFactor10 = scaleFactor10(mmHeight, signalRange) * 10;
      mean = (int) Math.rint(meanAndStdValues.get(variable)[0] * 1.0 / meanScaleFactor10) * meanScaleFactor10;
    }
    return new ScaleYInfo.Builder<>(variable).mean(mean)
        .scaleFactor(optimizeScaleY(mmHeight, signalRange))
        .scaleFactor10(scaleFactor10(mmHeight, signalRange)).build();
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
    return Math.max(1, scaleFactor);
  }
}
