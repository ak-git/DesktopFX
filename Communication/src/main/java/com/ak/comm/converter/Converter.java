package com.ak.comm.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;

public interface Converter<RESPONSE, EV extends Enum<EV> & Variable<EV>> extends Function<RESPONSE, Stream<int[]>> {
  @Nonnull
  List<EV> variables();

  @Nonnull
  Quantity<Frequency> getFrequency();
}
