package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Strings;

abstract class AbstractMediumLayers<D, T extends AbstractMediumLayers<D, T>> implements MediumLayers<D> {
  @Nonnull
  private final D rho;
  @Nonnull
  private final Collection<Prediction> predictions;

  AbstractMediumLayers(@Nonnull AbstractMediumBuilder<D, T> builder) {
    rho = builder.rho;
    predictions = builder.predictions;
  }

  @Override
  public final D rho() {
    return rho;
  }

  @Override
  public final double[] getInequalityL2() {
    return predictions.stream().map(Prediction::getInequalityL2)
        .reduce((doubles, doubles2) -> {
          var merge = new double[Math.max(doubles.length, doubles2.length)];
          for (var i = 0; i < merge.length; i++) {
            merge[i] = StrictMath.hypot(doubles[i], doubles2[i]);
          }
          return merge;
        }).orElseThrow();
  }

  @Nonnull
  final Collection<Prediction> getPredictions() {
    return Collections.unmodifiableCollection(predictions);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public String toString() {
    return "%s; L%s = %s %% %n%s".formatted(
        TetrapolarPrediction.toStringHorizons(TetrapolarPrediction.mergeHorizons(predictions)),
        Strings.low(2), Arrays.stream(getInequalityL2()).map(Metrics::toPercents).mapToObj("%.1f"::formatted).
            collect(Collectors.joining("; ", "[", "]")),
        predictions.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)));
  }

  abstract static class AbstractMediumBuilder<D, T extends AbstractMediumLayers<D, T>>
      implements Builder<T> {
    @Nonnull
    private final Collection<Prediction> predictions;
    D rho;

    AbstractMediumBuilder(@Nonnull Collection<Prediction> predictions) {
      this.predictions = Collections.unmodifiableCollection(predictions);
    }
  }
}
