package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

final class PeakToPeakFilter extends AbstractBufferFilter {
  private final Index max;
  private final Index min;

  PeakToPeakFilter(@Nonnegative int size) {
    super(size);
    max = new Index(Operator.MAX);
    min = new Index(Operator.MIN);
  }

  @Nonnegative
  @Override
  public double getDelay() {
    return 0.0;
  }

  @Override
  int apply(@Nonnegative int nowIndex) {
    return max.applyAsInt(nowIndex) - min.applyAsInt(nowIndex);
  }

  private enum Operator {
    MAX {
      @Override
      public boolean is(int now, int candidate) {
        return now <= candidate;
      }
    },
    MIN {
      @Override
      boolean is(int now, int candidate) {
        return now >= candidate;
      }
    };

    abstract boolean is(int now, int candidate);
  }

  private final class Index implements IntUnaryOperator {
    private final Operator operator;
    @Nonnegative
    private int extremalIndex;

    private Index(Operator operator) {
      this.operator = Objects.requireNonNull(operator);
    }

    @Override
    public int applyAsInt(int nowIndex) {
      if (nowIndex == extremalIndex) {
        int extremal = get(0);
        for (var i = 0; i < length(); i++) {
          int candidate = get(i);
          if (operator.is(extremal, candidate)) {
            extremal = candidate;
            extremalIndex = i;
          }
        }
      }
      else if (operator.is(get(extremalIndex), get(nowIndex))) {
        extremalIndex = nowIndex;
      }
      return get(extremalIndex);
    }
  }
}