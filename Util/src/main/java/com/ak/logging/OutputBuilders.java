package com.ak.logging;

import com.ak.util.Extension;
import com.ak.util.LocalIO;

public enum OutputBuilders {
  NONE {
    @Override
    public LocalIO build(String fileName) {
      return new OutputBuilder(Extension.NONE).fileName(fileName).build();
    }
  },
  CSV {
    @Override
    public LocalIO build(String fileName) {
      return new OutputBuilder(Extension.CSV).fileName(fileName).build();
    }
  };

  public abstract LocalIO build(String fileName);
}
