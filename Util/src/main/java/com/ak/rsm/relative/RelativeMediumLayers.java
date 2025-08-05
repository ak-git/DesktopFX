package com.ak.rsm.relative;

import com.ak.math.ValuePair;
import com.ak.rsm.system.Layers;
import com.ak.util.Strings;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.stream.Stream;

public record RelativeMediumLayers(ValuePair k, ValuePair hToL) {
  public static final RelativeMediumLayers SINGLE_LAYER = new RelativeMediumLayers(0.0, Double.NaN);
  public static final RelativeMediumLayers NAN = new RelativeMediumLayers(Double.NaN, Double.NaN);

  public RelativeMediumLayers(double k, double hToL) {
    this(ValuePair.Name.K12.of(k, 0.0), ValuePair.Name.H_L.of(Math.abs(hToL), 0.0));
  }

  public RelativeMediumLayers(double[] kw) {
    this(kw[0], kw[1]);
    if (kw.length > 2) throw new IllegalArgumentException(Arrays.toString(kw));
  }

  public RelativeMediumLayers(double[] rho, double hToL) {
    this(Layers.getK12(rho[0], rho[1]), hToL);
  }

  public int size() {
    return Stream.of(k, hToL).mapToDouble(ValuePair::value)
        .anyMatch(((DoublePredicate) Double::isNaN).or(x -> Double.compare(x, 0.0) == 0)) ? 1 : 2;
  }

  public String toString() {
    if (Double.isNaN(k.value())) {
      return String.valueOf(Double.NaN);
    }
    else if (size() == 1) {
      return Strings.EMPTY;
    }
    return "%s; %s".formatted(k, hToL);
  }
}
