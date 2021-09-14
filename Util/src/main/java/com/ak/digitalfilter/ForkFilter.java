package com.ak.digitalfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.ak.util.Strings.EMPTY;
import static com.ak.util.Strings.NEW_LINE;

final class ForkFilter extends AbstractDigitalFilter {
  private final List<DigitalFilter> filters = new LinkedList<>();

  ForkFilter(@Nonnull DigitalFilter[] filters) {
    if (filters.length < 2) {
      throw new IllegalArgumentException(Arrays.deepToString(filters));
    }
    this.filters.addAll(equalizeDelay(filters));

    int[] bufferPositions = getBufferPositions(this.filters);
    var bufferIndexes = new int[this.filters.size()];

    var initializedFlag = new AtomicBoolean();
    List<int[]> intBuffers = new ArrayList<>();
    for (var i = 0; i < this.filters.size(); i++) {
      DigitalFilter filter = this.filters.get(i);
      int filterI = i;
      filter.forEach(values -> {
        int bufferIndex = bufferIndexes[filterI];
        bufferIndexes[filterI]++;

        if (bufferIndex >= intBuffers.size()) {
          if (initializedFlag.get()) {
            throw new IllegalStateException("Invalid fork [ %s ] for filter {%n%s%n}, values = %s"
                .formatted(filter, this, Arrays.toString(values)));
          }
          else {
            intBuffers.add(new int[getOutputDataSize()]);
          }
        }

        System.arraycopy(values, 0, intBuffers.get(bufferIndex), bufferPositions[filterI], values.length);

        if (IntStream.of(bufferIndexes).allMatch(value -> value > 0)) {
          IntStream.of(bufferIndexes).filter(value -> value != bufferIndexes[0])
              .findAny()
              .ifPresentOrElse(
                  v -> initializedFlag.set(true),
                  () -> {
                    intBuffers.forEach(this::publish);
                    Arrays.fill(bufferIndexes, 0);
                    intBuffers.clear();
                    initializedFlag.set(false);
                  }
              );
        }
      });
    }
  }

  @Override
  public double getDelay() {
    return findMax(filters, DigitalFilter::getDelay);
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return findMax(filters, DigitalFilter::getFrequencyFactor);
  }

  @Override
  public void accept(@Nonnull int... in) {
    filters.forEach(filter -> filter.accept(in));
  }

  @Override
  public void reset() {
    filters.forEach(DigitalFilter::reset);
  }

  @Nonnegative
  @Override
  public int getOutputDataSize() {
    return filters.stream().mapToInt(DigitalFilter::getOutputDataSize).sum();
  }

  @Override
  public String toString() {
    return filters.stream().map(Object::toString).collect(Collectors.joining(NEW_LINE, EMPTY, EMPTY));
  }

  @Nonnull
  private static List<DigitalFilter> equalizeDelay(@Nonnull DigitalFilter[] filters) {
    List<DigitalFilter> filterList = Arrays.asList(filters);
    double maxDelay = findMax(filterList, DigitalFilter::getDelay);

    return filterList.stream()
        .map(filter -> {
          int delay = (int) Math.round(maxDelay - filter.getDelay());
          if (delay == 0) {
            return filter;
          }
          else {
            return new DelayFilter(filter, delay);
          }
        })
        .toList();
  }

  @Nonnull
  private static int[] getBufferPositions(@Nonnull List<DigitalFilter> filters) {
    var bufferPositions = new int[filters.size()];
    bufferPositions[0] = 0;
    for (var i = 1; i < filters.size(); i++) {
      bufferPositions[i] = bufferPositions[i - 1] + filters.get(i - 1).getOutputDataSize();
    }
    return bufferPositions;
  }

  @ParametersAreNonnullByDefault
  private static double findMax(Collection<DigitalFilter> filters, ToDoubleFunction<? super DigitalFilter> mapper) {
    return filters.stream().mapToDouble(mapper).max().orElseThrow(IllegalStateException::new);
  }
}
