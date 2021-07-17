package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import com.ak.util.Strings;

import static com.ak.rsm.Measurements.getRho1;

abstract class AbstractMediumLayers implements MediumLayers {
  @Nonnull
  private final RelativeMediumLayers kw;
  @Nonnull
  private final ValuePair rho;
  @Nonnull
  private final Collection<Measurement> measurements;
  @Nonnull
  private final Collection<Prediction> predictions;

  @ParametersAreNonnullByDefault
  AbstractMediumLayers(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    this.kw = kw;
    rho = getRho1(measurements, kw);
    this.measurements = Collections.unmodifiableCollection(measurements);
    double baseL = Measurements.getBaseL(measurements);
    predictions = measurements.stream()
        .map(m ->
            m.toPrediction(
                new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / m.getSystem().getL()),
                rho.getValue()
            )
        )
        .toList();
  }

  @Override
  public final ValuePair rho() {
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

  @Override
  @OverridingMethodsMustInvokeSuper
  public String toString() {
    var data = new StringJoiner(Strings.EMPTY);
    Iterator<Measurement> mIterator = measurements.iterator();
    Iterator<Prediction> pIterator = predictions.iterator();
    while (mIterator.hasNext() && pIterator.hasNext()) {
      data.add(mIterator.next().toString()).add("; %s".formatted(pIterator.next())).add(Strings.NEW_LINE);
    }

    return "%s; %s; L%s = %s %% %n%s".formatted(
        kw,
        TetrapolarPrediction.toStringHorizons(TetrapolarPrediction.mergeHorizons(predictions)), Strings.low(2),
        Arrays.stream(getInequalityL2()).map(Metrics::toPercents).mapToObj("%.1f"::formatted).collect(Collectors.joining("; ", "[", "]")),
        data);
  }
}
