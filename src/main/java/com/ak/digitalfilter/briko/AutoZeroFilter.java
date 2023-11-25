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
  private int counts;
  private int y;

  public AutoZeroFilter(@Nonnegative int settingCountsAndSkip) {
    this.settingCountsAndSkip = Math.abs(settingCountsAndSkip);
    rrsFilter.forEach(avg -> y = avg[0]);
  }

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      counts = 0;
      rrsFilter.reset();
    }
    if (counts < settingCountsAndSkip * 2) {
      if (counts < settingCountsAndSkip) {
        y = in;
      }
      else {
        rrsFilter.accept(in);
      }
      counts++;
    }
    return in - y;
  }
}