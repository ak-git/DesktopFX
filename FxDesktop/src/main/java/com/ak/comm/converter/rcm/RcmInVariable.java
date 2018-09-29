package com.ak.comm.converter.rcm;

import java.util.Collections;
import java.util.Set;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum RcmInVariable implements Variable<RcmInVariable> {
  RHEO_1 {
    @Override
    public DigitalFilter filter() {
      return toSignedFilter(-1);
    }
  },
  BASE_1,
  RHEO_2,
  ECG {
    @Override
    public DigitalFilter filter() {
      return toSignedFilter(1);
    }
  },
  BASE_2,
  RHEO_1X {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  QS_1,
  RHEO_2X {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  ECG_X {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  QS_2;


  static DigitalFilter toSignedFilter(int k) {
    return FilterBuilder.of().operator(() -> n -> {
      if ((n & 0x0800) == 0x0800) {
        n |= 0xfffff000;
      }
      return k * n;
    }).build();
  }
}
