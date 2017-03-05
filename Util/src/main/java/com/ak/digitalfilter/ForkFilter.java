package com.ak.digitalfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ak.util.Strings.EMPTY;
import static com.ak.util.Strings.NEW_LINE;

final class ForkFilter extends AbstractDigitalFilter {
  private final List<DigitalFilter> filters = new LinkedList<>();
  private final boolean parallel;

  ForkFilter(@Nonnull DigitalFilter[] filters, boolean parallel) {
    Objects.requireNonNull(filters);
    if (filters.length < 2) {
      throw new IllegalArgumentException(Arrays.deepToString(filters));
    }
    this.parallel = parallel;
    this.filters.addAll(Arrays.asList(filters));

    double maxDelay = getDelay();
    ListIterator<DigitalFilter> listIterator = this.filters.listIterator();
    while (listIterator.hasNext()) {
      DigitalFilter filter = listIterator.next();
      int delay = (int) Math.round(maxDelay - filter.getDelay());
      if (delay != 0) {
        listIterator.set(new ChainFilter(new DelayFilter(delay), filter));
      }
    }

    int[] bufferPositions = new int[this.filters.size()];
    bufferPositions[0] = 0;
    for (int i = 1; i < this.filters.size(); i++) {
      bufferPositions[i] = bufferPositions[i - 1] + this.filters.get(i - 1).size();
    }
    int[] bufferIndexes = new int[this.filters.size()];

    AtomicBoolean initializedFlag = new AtomicBoolean();
    List<int[]> intBuffers = new ArrayList<>();
    for (int i = 0; i < this.filters.size(); i++) {
      DigitalFilter filter = this.filters.get(i);
      int filterI = i;
      filter.forEach(values -> {
        int bufferIndex = bufferIndexes[filterI];
        bufferIndexes[filterI]++;

        if (bufferIndex >= intBuffers.size()) {
          if (initializedFlag.get()) {
            throw new IllegalStateException(String.format("Invalid fork [ %s ] for filter {%n%s%n}, values = %s", filter,
                this, Arrays.toString(values)));
          }
          else {
            intBuffers.add(new int[size()]);
          }
        }

        System.arraycopy(values, 0, intBuffers.get(bufferIndex), bufferPositions[filterI], values.length);

        if (IntStream.of(bufferIndexes).allMatch(value -> value > 0)) {
          initializedFlag.set(true);
          if (IntStream.of(bufferIndexes).allMatch(value -> value == bufferIndexes[0])) {
            intBuffers.forEach(this::publish);
            Arrays.fill(bufferIndexes, 0);
            intBuffers.clear();
            initializedFlag.set(false);
          }
        }
      });
    }
  }

  @Override
  public double getDelay() {
    return findMax(DigitalFilter::getDelay);
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return findMax(DigitalFilter::getFrequencyFactor);
  }

  @Override
  public void accept(int... in) {
    if (parallel) {
      if (filters.size() == in.length) {
        for (int i = 0; i < in.length; i++) {
          filters.get(i).accept(in[i]);
        }
      }
      else {
        illegalArgumentException(in);
      }
    }
    else {
      filters.forEach(filter -> filter.accept(in));
    }
  }

  @Nonnegative
  @Override
  public int size() {
    return filters.stream().mapToInt(DigitalFilter::size).sum();
  }

  @Override
  public String toString() {
    return filters.stream().map(Object::toString).collect(Collectors.joining(NEW_LINE, EMPTY, EMPTY));
  }

  private double findMax(@Nonnull ToDoubleFunction<? super DigitalFilter> mapper) {
    return filters.stream().mapToDouble(mapper).max().orElseThrow(IllegalStateException::new);
  }
}
