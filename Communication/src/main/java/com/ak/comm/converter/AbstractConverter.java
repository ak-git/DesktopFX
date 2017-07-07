package com.ak.comm.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.Filters;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_VALUES;

public abstract class AbstractConverter<RESPONSE, EV extends Enum<EV> & Variable<EV>> implements Converter<RESPONSE, EV> {
  @Nonnull
  private final Logger logger = Logger.getLogger(getClass().getName());
  @Nonnull
  private final List<EV> variables;
  @Nonnull
  private final DigitalFilter digitalFilter;
  @Nonnull
  private final Quantity<Frequency> frequency;
  @Nonnull
  private Stream<int[]> filteredValues = Stream.empty();

  public AbstractConverter(@Nonnull Class<EV> evClass, @Nonnull Quantity<Frequency> frequency) {
    this(evClass, frequency, EnumSet.allOf(evClass).stream().map(ev -> new int[] {ev.ordinal()}).collect(Collectors.toList()));
  }

  AbstractConverter(@Nonnull Class<EV> evClass, @Nonnull Quantity<Frequency> frequency, @Nonnull List<int[]> selectedIndexes) {
    variables = Collections.unmodifiableList(new ArrayList<>(EnumSet.allOf(evClass)));
    List<DigitalFilter> filters = variables.stream().map(ev -> ev.filter()).collect(Collectors.toList());

    digitalFilter = FilterBuilder.parallel(selectedIndexes, filters.toArray(new DigitalFilter[variables.size()]));
    digitalFilter.forEach(ints -> {
      if (logger.isLoggable(LOG_LEVEL_VALUES)) {
        logger.log(LOG_LEVEL_VALUES, String.format("#%x [ %s ]", hashCode(),
            IntStream.iterate(0, operand -> operand + 1).limit(variables.size()).mapToObj(
                idx -> Variables.toString(variables.get(idx), ints[idx])).collect(Collectors.joining(", "))));
      }
      filteredValues = Stream.concat(filteredValues, Stream.of(ints));
    });
    this.frequency = Filters.getFrequency(digitalFilter, frequency);
  }

  @Override
  public final List<EV> variables() {
    return variables;
  }

  @Nonnegative
  @Override
  public final Quantity<Frequency> getFrequency() {
    return frequency;
  }

  @Override
  public final Stream<int[]> apply(@Nonnull RESPONSE response) {
    Objects.requireNonNull(response);
    filteredValues = Stream.empty();
    innerApply(response).peek(ints -> {
      if (ints.length < variables.size()) {
        logger.log(Level.WARNING, String.format("Invalid variables: %s not match %s", variables, Arrays.toString(ints)));
      }
    }).forEach(digitalFilter::accept);
    return filteredValues;
  }

  @Nonnull
  protected abstract Stream<int[]> innerApply(@Nonnull RESPONSE response);
}
