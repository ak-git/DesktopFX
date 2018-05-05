package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Unit;
import javax.measure.quantity.Length;

final class TetrapolarSystemPair {
  private final TetrapolarSystem[] pair;

  TetrapolarSystemPair(@Nonnegative double s1PU, @Nonnegative double s2PU, @Nonnegative double lCC, @Nonnull Unit<Length> unit) {
    pair = new TetrapolarSystem[2];
    pair[0] = new TetrapolarSystem(Math.min(s1PU, s2PU), lCC, unit);
    pair[1] = new TetrapolarSystem(Math.max(s1PU, s2PU), lCC, unit);
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
}
