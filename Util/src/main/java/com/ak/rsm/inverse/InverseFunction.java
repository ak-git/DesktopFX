package com.ak.rsm.inverse;

import com.ak.rsm.relative.RelativeMediumLayers;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public interface InverseFunction extends ToDoubleFunction<double[]>, Function<double[], RelativeMediumLayers> {
}
