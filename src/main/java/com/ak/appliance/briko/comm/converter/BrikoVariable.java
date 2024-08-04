package com.ak.appliance.briko.comm.converter;

import com.ak.comm.converter.Variable;

import java.util.Collections;
import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  EMG1,
  EMG2,
  EMG3,
  EMG4,
  EMG5,
  EMG6,
  EMG7 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  EMG8 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  EMG9 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  EMG10 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  EMG11 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  EMG12 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R1,
  R2,
  R3,
  R4,
  R5,
  R6,
  R7 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R8 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R9 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R10 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R11 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R12 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  };

  public static final int FREQUENCY = 512;
}
