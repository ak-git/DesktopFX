package com.ak.appliance.rcm.comm.converter;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

import java.util.Collections;
import java.util.Set;

public enum RcmInVariable implements Variable<RcmInVariable> {
  RHEO_1 {
    @Override
    public DigitalFilter filter() {
      return toSignedFilter();
    }
  },
  BASE_1,
  RHEO_2,
  ECG {
    @Override
    public DigitalFilter filter() {
      return toSignedFilter();
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


  static DigitalFilter toSignedFilter() {
    return FilterBuilder.of().operator(() -> n -> {
      if ((n & 0x0800) == 0x0800) {
        n |= 0xfffff000;
      }
      return n;
    }).build();
  }
}
