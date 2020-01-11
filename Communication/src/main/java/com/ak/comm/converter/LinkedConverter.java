package com.ak.comm.converter;

import java.util.List;
import java.util.stream.Stream;

public final class LinkedConverter<R, I extends Enum<I> & Variable<I>, O extends Enum<O> & DependentVariable<I, O>>
    implements Converter<R, O> {
  private final Converter<R, I> responseConverter;
  private final Converter<Stream<int[]>, O> outConverter;

  public LinkedConverter(Converter<R, I> responseConverter, Class<O> outVarClass) {
    this.responseConverter = responseConverter;
    outConverter = new SelectableConverter<>(outVarClass, responseConverter.getFrequency());
  }

  @Override
  public List<O> variables() {
    return outConverter.variables();
  }

  @Override
  public double getFrequency() {
    return outConverter.getFrequency();
  }

  @Override
  public Stream<int[]> apply(R response) {
    return responseConverter.apply(response).flatMap(ints -> outConverter.apply(Stream.of(ints)));
  }

  @Override
  public void refresh() {
    responseConverter.refresh();
    outConverter.refresh();
  }
}
