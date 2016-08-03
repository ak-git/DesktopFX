package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class ForkFilter extends AbstractDigitalFilter {
  @Nonnull
  private final DigitalFilter left;
  @Nonnull
  private final DigitalFilter right;

  ForkFilter(@Nonnull DigitalFilter left, @Nonnull DigitalFilter right) {
    int delay = (int) Math.round(Math.abs(left.getDelay() - right.getDelay()));
    if (left.getDelay() > right.getDelay()) {
      this.left = left;
      this.right = new ChainFilter(right, new DelayFilter(delay));
    }
    else if (left.getDelay() < right.getDelay()) {
      this.left = new ChainFilter(left, new DelayFilter(delay));
      this.right = right;
    }
    else {
      this.left = left;
      this.right = right;
    }

    AtomicBoolean sync = new AtomicBoolean();
    int[] result = new int[size()];
    this.left.forEach(values -> {
      if (sync.get()) {
        throw new IllegalStateException(Arrays.toString(values));
      }
      else {
        System.arraycopy(values, 0, result, 0, values.length);
        sync.set(true);
      }
    });
    this.right.forEach(values -> {
      if (sync.get()) {
        System.arraycopy(values, 0, result, result.length - values.length, values.length);
        publish(result);
        sync.set(false);
      }
      else {
        throw new IllegalStateException(Arrays.toString(values));
      }
    });
  }

  @Nonnegative
  @Override
  public double getDelay() {
    return Math.max(left.getDelay(), right.getDelay());
  }

  @Override
  public void accept(int... in) {
    left.accept(in);
    right.accept(in);
  }

  @Nonnegative
  @Override
  public int size() {
    return left.size() + right.size();
  }
}
