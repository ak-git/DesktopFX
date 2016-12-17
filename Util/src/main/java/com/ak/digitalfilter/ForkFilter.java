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

  ForkFilter(@Nonnull DigitalFilter first, @Nonnull DigitalFilter... next) {
    Objects.requireNonNull(next);
    filters.add(first);
    filters.addAll(Arrays.asList(next));

    double maxDelay = getDelay();
    ListIterator<DigitalFilter> listIterator = filters.listIterator();
    while (listIterator.hasNext()) {
      DigitalFilter filter = listIterator.next();
      int delay = (int) Math.round(maxDelay - filter.getDelay());
      if (delay != 0) {
        listIterator.set(new ChainFilter(filter, new DelayFilter(delay)));
      }
    }

    IntBuffer buffer = IntBuffer.allocate(size());
    AtomicInteger sync = new AtomicInteger();
    for (int i = 0; i < filters.size(); i++) {
      DigitalFilter filter = filters.get(i);
      int finalI = i;
      filter.forEach(values -> {
        if (sync.compareAndSet(finalI, finalI + 1)) {
          buffer.put(values);
          if (finalI == filters.size() - 1) {
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
    filters.forEach(filter -> filter.accept(in));
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
