package com.ak.rsm.inverse;

import com.ak.rsm.relative.RelativeMediumLayers;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

sealed interface Inverse<L> extends Supplier<L>, UnaryOperator<RelativeMediumLayers>
    permits AbstractRelative, DynamicAbsolute, StaticAbsolute {
}

