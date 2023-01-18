package com.ak.rsm.inverse;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.ak.rsm.relative.RelativeMediumLayers;

interface Inverse<L> extends Supplier<L>, UnaryOperator<RelativeMediumLayers> {
}

