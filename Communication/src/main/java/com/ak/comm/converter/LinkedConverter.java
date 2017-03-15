package com.ak.comm.converter;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

final class LinkedConverter<RESPONSE, IN extends Enum<IN> & Variable, OUT extends Enum<OUT> & DependentVariable<IN>>
    implements Converter<RESPONSE, OUT> {
  private final Converter<RESPONSE, IN> responseConverter;
  private final Converter<Stream<int[]>, OUT> outConverter;

  LinkedConverter(Converter<RESPONSE, IN> responseConverter, Class<OUT> outVarClass) {
    this.responseConverter = responseConverter;
    outConverter = new SelectableConverter<>(outVarClass);
  }

  @Nonnull
  @Override
  public List<OUT> variables() {
    return outConverter.variables();
  }

  @Override
  public Stream<int[]> apply(RESPONSE response) {
    return responseConverter.apply(response).flatMap(ints -> outConverter.apply(Stream.of(ints)));
  }
}
