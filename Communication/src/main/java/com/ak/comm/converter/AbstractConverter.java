package com.ak.comm.converter;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;

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

  protected AbstractConverter(@Nonnull Class<V> evClass, @Nonnegative double frequency) {
    this(evClass, frequency, EnumSet.allOf(evClass).stream().map(v -> new int[] {v.ordinal()}).toList());
  }

  AbstractConverter(@Nonnull Class<V> evClass, @Nonnegative double frequency, @Nonnull List<int[]> selectedIndexes) {
    variables = List.copyOf(EnumSet.allOf(evClass));
    List<DigitalFilter> filters = variables.stream().map(Variable::filter).toList();

    digitalFilter = FilterBuilder.parallel(selectedIndexes, filters.toArray(new DigitalFilter[variables.size()]));
    digitalFilter.forEach(ints -> {
      logger.log(LOG_LEVEL_VALUES, () -> "#%08x [ %s ]".formatted(hashCode(),
          variables.stream().map(v -> Variables.toString(v, ints[v.ordinal()])).collect(Collectors.joining("; ")))
      );
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
  @OverridingMethodsMustInvokeSuper
  public void refresh(boolean force) {
    digitalFilter.reset();
  }

  @Nonnull
  protected abstract Stream<int[]> innerApply(@Nonnull R response);
}
