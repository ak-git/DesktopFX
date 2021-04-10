package com.ak.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

final class CSVMultiFileCollector<T> implements Collector<Stream<T>, List<CSVLineFileCollector>, Boolean> {
  @Nonnull
  private final Collection<String> paths = new ArrayList<>();
  @Nonnull
  private final List<Function<T, Object>> functions = new ArrayList<>();

  @Override
  public Supplier<List<CSVLineFileCollector>> supplier() {
    return () -> paths.stream().map(CSVLineFileCollector::new).collect(Collectors.toList());
  }

  @Override
  public BiConsumer<List<CSVLineFileCollector>, Stream<T>> accumulator() {
    return (lineFileCollectors, inStream) -> {
      Collection<T> pairs = inStream.collect(Collectors.toUnmodifiableList());
      for (int i = 0; i < lineFileCollectors.size(); i++) {
        lineFileCollectors.get(i).accept(pairs.stream().map(functions.get(i)).toArray());
      }
    };
  }

  @Override
  public BinaryOperator<List<CSVLineFileCollector>> combiner() {
    return (c1, c2) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  public Function<List<CSVLineFileCollector>, Boolean> finisher() {
    return lineFileCollectors -> {
      AtomicBoolean okFlag = new AtomicBoolean(true);
      lineFileCollectors.forEach(lineFileCollector -> {
        try {
          lineFileCollector.close();
        }
        catch (IOException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
          okFlag.set(false);
        }
      });
      return okFlag.get();
    };
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  public static final class Builder<T> implements com.ak.util.Builder<CSVMultiFileCollector<T>> {
    private final CSVMultiFileCollector<T> multiFileCollector = new CSVMultiFileCollector<>();

    @ParametersAreNonnullByDefault
    public Builder<T> add(String outFileName, Function<T, Object> converter) {
      multiFileCollector.paths.add(outFileName);
      multiFileCollector.functions.add(converter);
      return this;
    }

    @Override
    public CSVMultiFileCollector<T> build() {
      return multiFileCollector;
    }
  }
}
