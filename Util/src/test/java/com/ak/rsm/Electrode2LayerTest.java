package com.ak.rsm;

import java.util.function.ToDoubleFunction;

import com.ak.math.ValuePair;

public class Electrode2LayerTest {
  private static final double OVERALL_DIM = 1.0;
  private static final double REL_ERROR_OVERALL_DIM = 1.0e-6;
  private static final double ABS_ERROR_OVERALL_DIM = REL_ERROR_OVERALL_DIM * OVERALL_DIM;
  private static final ToDoubleFunction<RelativeMediumLayers<ValuePair>> K =
      value -> Math.abs(value.k12().getAbsError() / value.k12().getValue()) / REL_ERROR_OVERALL_DIM;
  private static final ToDoubleFunction<RelativeMediumLayers<ValuePair>> H =
      value -> value.hToL().getAbsError() / OVERALL_DIM / REL_ERROR_OVERALL_DIM;
}
