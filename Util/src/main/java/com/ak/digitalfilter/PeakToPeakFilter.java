package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class PeakToPeakFilter extends AbstractBufferFilter {
  @Nonnull
  private final Index max;
  @Nonnull
  private final Index min;

  PeakToPeakFilter(@Nonnegative int size) {
    super(size);
    max = new Index(Operator.MAX);
    min = new Index(Operator.MIN);
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
    @Nonnull
    private final Operator operator;
    @Nonnegative
    private int extremalIndex;

    private Index(@Nonnull Operator operator) {
      this.operator = operator;
    }

    @Override
    public int applyAsInt(int nowIndex) {
      if (nowIndex == extremalIndex) {
        int extremal = get(0);
        for (int i = 0; i < length(); i++) {
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