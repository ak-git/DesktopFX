package com.ak.digitalfilter;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

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

    IntBuffer buffer = IntBuffer.allocate(size());
    AtomicInteger sync = new AtomicInteger();
    for (int i = 0; i < this.filters.size(); i++) {
      DigitalFilter filter = this.filters.get(i);
      int finalI = i;
      filter.forEach(values -> {
        if (sync.compareAndSet(finalI, finalI + 1)) {
          buffer.put(values);
          if (finalI == this.filters.size() - 1) {
            buffer.flip();
            publish(buffer.array());
            buffer.clear();
            sync.set(0);
          }
        }
        else {
          throw new IllegalStateException(String.format("Invalid fork [ %s ] for filter {%n%s%n}, values = %s", filter,
              this, Arrays.toString(values)));
        }
      });
    }
  }

  @Override
  public double getDelay() {
    return findMax(Delay::getDelay);
  }

  @Nonnegative
  @Override
  public double getFrequencyFactor() {
    return findMax(Delay::getFrequencyFactor);
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
