package com.ak.comm.converter;

import java.util.List;
import java.util.stream.Stream;

public final class LinkedConverter<R, I extends Enum<I> & Variable<I>, O extends Enum<O> & DependentVariable<I, O>>
    implements Converter<R, O> {
  private final Converter<R, I> responseConverter;
  private final Converter<Stream<int[]>, O> outConverter;

  private LinkedConverter(Converter<R, I> responseConverter, Class<O> outVarClass) {
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

  public static <R, I extends Enum<I> & Variable<I>, O extends Enum<O> & DependentVariable<I, O>>
  LinkedConverter<R, I, O> of(Converter<R, I> responseConverter, Class<O> outVarClass) {
    return new LinkedConverter<>(responseConverter, outVarClass);
  }

  public <O2 extends Enum<O2> & DependentVariable<O, O2>> LinkedConverter<R, O, O2> chainInstance(Class<O2> outVarClass) {
    return new LinkedConverter<>(this, outVarClass);
  }
}
