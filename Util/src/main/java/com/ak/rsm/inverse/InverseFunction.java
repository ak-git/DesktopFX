package com.ak.rsm.inverse;

import com.ak.rsm.relative.RelativeMediumLayers;

import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

public interface InverseFunction extends ToDoubleFunction<double[]>, UnaryOperator<RelativeMediumLayers> {
}
