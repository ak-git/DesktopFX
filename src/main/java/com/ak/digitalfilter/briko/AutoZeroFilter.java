package com.ak.digitalfilter.briko;

import com.ak.digitalfilter.AbstractOperableFilter;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.RRSFilter;

import javax.annotation.Nonnegative;

public final class AutoZeroFilter extends AbstractOperableFilter {
  private final DigitalFilter rrsFilter = new RRSFilter();
  @Nonnegative
  private final int settingCountsAndSkip;
  @Nonnegative
  private int countCounts;
  private int y;

  public AutoZeroFilter(@Nonnegative int settingCountsAndSkip) {
    this.settingCountsAndSkip = Math.abs(settingCountsAndSkip);
    rrsFilter.forEach(avg -> y = avg[0]);
  }

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      countCounts = 0;
      rrsFilter.reset();
    }
    if (countCounts < settingCountsAndSkip * 2) {
      if (countCounts < settingCountsAndSkip) {
        y = in;
      }
      else {
        rrsFilter.accept(in);
      }
      countCounts++;
    }
    return in - y;
  }
}