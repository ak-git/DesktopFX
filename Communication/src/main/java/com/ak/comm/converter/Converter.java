package com.ak.comm.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public interface Converter<RESPONSE, EV extends Enum<EV> & Variable<EV>> extends Function<RESPONSE, Stream<int[]>> {
  @Nonnull
  List<EV> variables();

  default DigitalFilter filter() {
    DigitalFilter first = FilterBuilder.of().build();
    if (variables().size() > 1) {
      DigitalFilter[] filters = new DigitalFilter[variables().size() - 1];
      for (int i = 0; i < filters.length; i++) {
        filters[i] = FilterBuilder.of().build();
      }
      return FilterBuilder.of().fork(first, filters).build();
    }
    else {
      return first;
    }
  }
}
