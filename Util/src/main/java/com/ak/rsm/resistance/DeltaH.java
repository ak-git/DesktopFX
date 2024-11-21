package com.ak.rsm.resistance;

import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public interface DeltaH {
  DeltaH NULL = new Type.Value(Type.NONE, Double.NaN);
  DoubleFunction<DeltaH> H1 = value -> new Type.Value(Type.H1, value);
  DoubleFunction<DeltaH> H2 = value -> new Type.Value(Type.H2, value);

  enum Type {
    NONE, H1, H2;

    private record Value(Type type, double value) implements DeltaH {
      private Value(Type type, double value) {
        this.type = Objects.requireNonNull(type);
        if (type == NONE) {
          this.value = Double.NaN;
        }
        else if (Double.isFinite(value)) {
          this.value = value;
        }
        else {
          throw new IllegalArgumentException("Value is not finite = %f".formatted(value));
        }
      }

      @Override
      public DeltaH convert(DoubleUnaryOperator converter) {
        return new Value(type, Objects.requireNonNull(converter).applyAsDouble(value));
      }
    }
  }

  Type type();

  double value();

  DeltaH convert(DoubleUnaryOperator converter);
}

