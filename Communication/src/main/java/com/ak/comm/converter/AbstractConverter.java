package com.ak.comm.converter;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.logging.OutputBuilders;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;

import static com.ak.util.LogUtils.LOG_LEVEL_VALUES;

public abstract class AbstractConverter<R, V extends Enum<V> & Variable<V>> implements Converter<R, V> {
  @Nonnull
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final List<V> variables;
  @Nonnull
  private final DigitalFilter digitalFilter;
  @Nonnegative
  private final double frequency;
  @Nonnull
  private Stream<int[]> filteredValues = Stream.empty();
  @Nullable
  private LineFileCollector fileCollector;

  protected AbstractConverter(@Nonnull Class<V> evClass, @Nonnegative double frequency) {
    this(evClass, frequency, EnumSet.allOf(evClass).stream().map(v -> new int[] {v.ordinal()}).collect(Collectors.toList()));
  }

  AbstractConverter(@Nonnull Class<V> evClass, @Nonnegative double frequency, @Nonnull List<int[]> selectedIndexes) {
    variables = List.copyOf(EnumSet.allOf(evClass));
    List<DigitalFilter> filters = variables.stream().map(Variable::filter).collect(Collectors.toList());

    digitalFilter = FilterBuilder.parallel(selectedIndexes, filters.toArray(new DigitalFilter[variables.size()]));
    digitalFilter.forEach(ints -> {
      if (logger.isLoggable(LOG_LEVEL_VALUES)) {
        logger.log(LOG_LEVEL_VALUES, "#%08x [ %s ]".formatted(hashCode(),
            IntStream.iterate(0, operand -> operand + 1).limit(variables.size()).mapToObj(
                idx -> Variables.toString(variables.get(idx), ints[idx])).collect(Collectors.joining(", "))));
      }
      try {
        if (fileCollector == null) {
          fileCollector = new LineFileCollector(OutputBuilders.build(getClass().getSimpleName()).getPath(), LineFileCollector.Direction.VERTICAL);
          fileCollector.accept(variables.stream().map(Variables::toName).collect(Collectors.joining(Strings.TAB)));
        }
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      }
      fileCollector.accept(Arrays.stream(ints).mapToObj(Integer::toString).collect(Collectors.joining(Strings.TAB)));
      filteredValues = Stream.concat(filteredValues, Stream.of(ints));
    });
    this.frequency = frequency * digitalFilter.getFrequencyFactor();
  }

  @Override
  public final List<V> variables() {
    return variables;
  }

  @Nonnegative
  @Override
  public final double getFrequency() {
    return frequency;
  }

  @Override
  public final Stream<int[]> apply(@Nonnull R response) {
    Objects.requireNonNull(response);
    filteredValues = Stream.empty();
    innerApply(response).forEach(digitalFilter::accept);
    return filteredValues;
  }

  @Override
  public final void refresh() {
    digitalFilter.reset();
    try {
      if (fileCollector != null) {
        fileCollector.close();
      }
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
    finally {
      fileCollector = null;
    }
  }

  @Nonnull
  protected abstract Stream<int[]> innerApply(@Nonnull R response);
}
