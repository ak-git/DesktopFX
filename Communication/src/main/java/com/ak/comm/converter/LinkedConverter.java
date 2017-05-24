package com.ak.comm.converter;

import java.util.List;
import java.util.stream.Stream;

public final class LinkedConverter<RESPONSE, IN extends Enum<IN> & Variable<IN>, OUT extends Enum<OUT> & DependentVariable<IN, OUT>>
    implements Converter<RESPONSE, OUT> {
  private final Converter<RESPONSE, IN> responseConverter;
  private final Converter<Stream<int[]>, OUT> outConverter;

  public LinkedConverter(Converter<RESPONSE, IN> responseConverter, Class<OUT> outVarClass) {
    this.responseConverter = responseConverter;
    outConverter = new SelectableConverter<>(outVarClass);
  }

  @Override
  public List<OUT> variables() {
    return outConverter.variables();
  }

  @Override
  public Stream<int[]> apply(RESPONSE response) {
    return responseConverter.apply(response).flatMap(ints -> outConverter.apply(Stream.of(ints)));
  }
}
