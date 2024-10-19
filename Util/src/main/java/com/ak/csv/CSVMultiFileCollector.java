package com.ak.csv;

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
  private static final Logger LOGGER = Logger.getLogger(CSVMultiFileCollector.class.getName());
  private final Collection<Path> paths = new ArrayList<>();
  private final List<Function<T, Object>> functions = new ArrayList<>();
  private final Iterator<Y> yVarIterator;
  private final String[] headers;

  private CSVMultiFileCollector(Iterator<Y> yVarIterator, String... headers) {
    this.yVarIterator = Objects.requireNonNull(yVarIterator);
    this.headers = headers.clone();
  }

  @Override
  public Supplier<List<CSVLineFileCollector>> supplier() {
    return () -> paths.stream().map(s -> new CSVLineFileCollector(s, headers)).toList();
  }

  @Override
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
  public BinaryOperator<List<CSVLineFileCollector>> combiner() {
    return (_, _) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  public Function<List<CSVLineFileCollector>, Boolean> finisher() {
    return lineFileCollectors -> {
      var okFlag = new AtomicBoolean(true);
      lineFileCollectors.forEach(lineFileCollector -> {
        try {
          lineFileCollector.close();
        }
        catch (IOException e) {
          LOGGER.log(Level.WARNING, e.getMessage(), e);
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

  static final class Builder<Y, T> implements com.ak.util.Builder<CSVMultiFileCollector<Y, T>> {
    private final CSVMultiFileCollector<Y, T> multiFileCollector;

    Builder(BaseStream<Y, Stream<Y>> yVar, String... headers) {
      multiFileCollector = new CSVMultiFileCollector<>(yVar.iterator(), headers);
    }

    Builder<Y, T> add(Path outFileName, Function<T, Object> converter) {
      multiFileCollector.paths.add(Objects.requireNonNull(outFileName));
      multiFileCollector.functions.add(Objects.requireNonNull(converter));
      return this;
    }

    @Override
    public CSVMultiFileCollector<Y, T> build() {
      return multiFileCollector;
    }
  }
}
