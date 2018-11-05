package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import tec.uom.se.quantity.Quantities;

import static tec.uom.se.unit.Units.METRE;

final class TetrapolarSystemPair {
  private final TetrapolarSystem[] pair;

  private TetrapolarSystemPair(@Nonnegative double sPUSmall, @Nonnegative double sPULarge, @Nonnegative double lCC, @Nonnull Unit<Length> unit) {
    double sMin = Math.min(sPUSmall, sPULarge);
    if (sMin <= 0) {
      throw new IllegalArgumentException(String.format("sPU[ %.1f ] <= 0", sMin));
    }

    double sMax = Math.max(sPUSmall, sPULarge);
    if (lCC <= sMax) {
      throw new IllegalArgumentException(String.format("lCC[ %.1f ] <= sPU [ %.1f ]", lCC, sMax));
    }

    pair = new TetrapolarSystem[2];
    pair[0] = new TetrapolarSystem(sMin, lCC, unit);
    pair[1] = new TetrapolarSystem(sMax, lCC, unit);
  }

  TetrapolarSystem[] getPair() {
    return Arrays.copyOf(pair, pair.length);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    TetrapolarSystemPair that = (TetrapolarSystemPair) o;
    return Arrays.equals(pair, that.pair);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(pair);
  }

  @Override
  public String toString() {
    return Arrays.stream(pair).map(TetrapolarSystem::toString).collect(Collectors.joining(" / "));
  }

  static class Builder implements com.ak.util.Builder<TetrapolarSystemPair> {
    @Nonnegative
    private final double dL;
    @Nonnull
    private final Unit<Length> unit;
    @Nonnegative
    double sPUSmall;
    @Nonnegative
    double sPULarge;
    @Nonnegative
    double lCC;

    Builder(@Nonnegative double dL, @Nonnull Unit<Length> unit) {
      this.dL = dL;
      this.unit = unit;
    }

    Builder sPU(@Nonnegative double sPUSmall, @Nonnegative double sPULarge) {
      this.sPUSmall = Math.min(sPUSmall, sPULarge);
      this.sPULarge = Math.max(sPUSmall, sPULarge);
      return this;
    }

    Builder lCC(@Nonnegative double lCC) {
      this.lCC = lCC;
      return this;
    }

    double getLCC() {
      return Quantities.getQuantity(lCC, unit).to(METRE).getValue().doubleValue();
    }

    TetrapolarSystemPair[] buildWithError() {
      if (Double.compare(dL, 0.0) == 0) {
        throw new IllegalStateException("dL == 0");
      }

      return IntStream.of(1, -1).mapToObj(dLSign -> {
            var error = dL * dLSign;
            return new TetrapolarSystemPair(sPUSmall + error, sPULarge - error, lCC + error, unit);
          }
      ).toArray(TetrapolarSystemPair[]::new);
    }

    @Override
    public TetrapolarSystemPair build() {
      return new TetrapolarSystemPair(sPUSmall, sPULarge, lCC, unit);
    }
  }
}
