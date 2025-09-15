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

    private record Value2(Value h1Value, double h2Value) implements DeltaH {
      @Override
      public Type type() {
        return h1Value.type;
      }

      @Override
      public double value() {
        return h1Value.value;
      }

      @Override
      public DeltaH convert(DoubleUnaryOperator converter) {
        return new Type.Value2(new Type.Value(Type.H1, converter.applyAsDouble(h1Value().value)), converter.applyAsDouble(h2Value));
      }

      @Override
      public DeltaH next() {
        return new Type.Value(Type.H2, h2Value);
      }
    }
  }

  Type type();

  double value();

  DeltaH convert(DoubleUnaryOperator converter);

  default DeltaH next() {
    return NULL;
  }

  static DeltaH ofH1andH2(double h1Value, double h2Value) {
    return new Type.Value2(new Type.Value(Type.H1, h1Value), h2Value);
  }
}

