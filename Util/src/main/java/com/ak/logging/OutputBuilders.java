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
  NONE_WITH_DATE {
    @Override
    public LocalIO build(String fileName) {
      return new OutputBuilder(Extension.NONE).addPathWithDate().fileName(fileName).build();
    }
  };

  public abstract LocalIO build(String fileName);
}
