package com.ak.util;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Stream;

final class CSVMultiFileCollector<Y, T> implements Collector<Stream<T>, List<CSVLineFileCollector>, Boolean> {
  @Nonnull
  private final Collection<Path> paths = new ArrayList<>();
  @Nonnull
  private final List<Function<T, Object>> functions = new ArrayList<>();
  @Nonnull
  private final Iterator<Y> yVarIterator;
  @Nonnull
  private final String[] headers;

  private CSVMultiFileCollector(@Nonnull Iterator<Y> yVarIterator, @Nonnull String... headers) {
    this.headers = headers.clone();
    this.yVarIterator = yVarIterator;
  }

  @Override
  @Nonnull
  public Supplier<List<CSVLineFileCollector>> supplier() {
    return () -> paths.stream().map(s -> new CSVLineFileCollector(s, headers)).toList();
  }

  @Override
  @Nonnull
  public BiConsumer<List<CSVLineFileCollector>, Stream<T>> accumulator() {
    return (lineFileCollectors, inStream) -> {
      Collection<T> pairs = inStream.toList();
      var y = yVarIterator.next();
      for (var i = 0; i < lineFileCollectors.size(); i++) {
        lineFileCollectors.get(i).accept(Stream.concat(Stream.of(y), pairs.stream().map(functions.get(i))).toArray());
      }
    };
  }

  @Override
  @Nonnull
  public BinaryOperator<List<CSVLineFileCollector>> combiner() {
    return (c1, c2) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  @Nonnull
  public Function<List<CSVLineFileCollector>, Boolean> finisher() {
    return lineFileCollectors -> {
      var okFlag = new AtomicBoolean(true);
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
  @Nonnull
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  static final class CollectorBuilder<Y, T> implements Builder<CSVMultiFileCollector<Y, T>> {
    @Nonnull
    private final CSVMultiFileCollector<Y, T> multiFileCollector;

    CollectorBuilder(@Nonnull BaseStream<Y, Stream<Y>> yVar, @Nonnull String... headers) {
      multiFileCollector = new CSVMultiFileCollector<>(yVar.iterator(), headers);
    }

    @ParametersAreNonnullByDefault
    CollectorBuilder<Y, T> add(Path outFileName, Function<T, Object> converter) {
      multiFileCollector.paths.add(outFileName);
      multiFileCollector.functions.add(converter);
      return this;
    }

    @Override
    @Nonnull
    public CSVMultiFileCollector<Y, T> build() {
      return multiFileCollector;
    }
  }
}
