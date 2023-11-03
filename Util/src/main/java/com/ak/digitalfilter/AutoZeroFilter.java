package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class AutoZeroFilter extends AbstractOperableFilter {
  private final DigitalFilter rrsFilter = new RRSFilter();
  @Nonnegative
  private final int settingCountsAndSkip;
  @Nonnegative
  private int countCounts;
  private int y;

  AutoZeroFilter(@Nonnegative int settingCountsAndSkip) {
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