package com.ak.comm;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Variable;

public interface ServiceReadable<EV extends Enum<EV> & Variable<EV>> {
  @Nonnull
  Map<EV, int[]> read(@Nonnegative int fromInclusive, @Nonnegative int toExclusive);

  @Nonnull
  List<EV> getVariables();

  @Nonnegative
  double getFrequency();
}
