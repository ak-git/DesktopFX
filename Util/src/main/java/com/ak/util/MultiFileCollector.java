package com.ak.util;

import java.io.IOException;
import java.nio.file.Path;
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
import java.util.function.ToDoubleFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

final class MultiFileCollector<IN> implements Collector<Stream<IN>, List<LineFileCollector>, Boolean> {
  @Nonnull
  private final String outFormat;
  @Nonnull
  private final Collection<Path> paths = new ArrayList<>();
  @Nonnull
  private final List<ToDoubleFunction<IN>> functions = new ArrayList<>();

  private MultiFileCollector(@Nonnull String outFormat) {
    this.outFormat = outFormat;
  }

  @Override
  public Supplier<List<LineFileCollector>> supplier() {
    return () -> paths.stream().
        map(path -> {
          try {
            return new LineFileCollector(path, LineFileCollector.Direction.VERTICAL);
          }
          catch (IOException e) {
            throw new AssertionError(e);
          }
        }).collect(Collectors.toList());
  }

  @Override
  public BiConsumer<List<LineFileCollector>, Stream<IN>> accumulator() {
    return (lineFileCollectors, inStream) -> {
      List<IN> pairs = inStream.collect(Collectors.toList());
      for (int i = 0; i < lineFileCollectors.size(); i++) {
        lineFileCollectors.get(i).accept(
            pairs.stream().mapToDouble(functions.get(i)).mapToObj(value -> String.format(outFormat, value)).
                collect(Collectors.joining(Strings.TAB))
        );
      }
    };
  }

  @Override
  public BinaryOperator<List<LineFileCollector>> combiner() {
    return (lineFileCollectors, lineFileCollectors2) -> {
      throw new UnsupportedOperationException();
    };
  }

  @Override
  public Function<List<LineFileCollector>, Boolean> finisher() {
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

  public static final class Builder<IN> implements com.ak.util.Builder<MultiFileCollector<IN>> {
    @Nonnull
    private final MultiFileCollector<IN> multiFileCollector;

    public Builder(@Nonnull String outFormat) {
      multiFileCollector = new MultiFileCollector<>(outFormat);
    }

    public Builder<IN> add(@Nonnull Path out, @Nonnull ToDoubleFunction<IN> converter) {
      multiFileCollector.paths.add(out);
      multiFileCollector.functions.add(converter);
      return this;
    }

    @Override
    public MultiFileCollector<IN> build() {
      return multiFileCollector;
    }
  }
}
