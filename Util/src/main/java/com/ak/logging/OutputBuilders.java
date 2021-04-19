package com.ak.logging;

import javax.annotation.Nonnull;

import com.ak.util.Extension;
import com.ak.util.LocalIO;

public enum OutputBuilders {
  SAVE {
    @Override
    public LocalIO build(@Nonnull String fileName) {
      return new OutputBuilder(Extension.CSV).addPathWithDate().fileNameWithDateTime(fileName).build();
    }
  },
  CONVERT {
    @Override
    public LocalIO build(@Nonnull String fileName) {
      return new OutputBuilder(Extension.CSV).fileName(fileName).build();
    }
  };

  public abstract LocalIO build(@Nonnull String fileName);
}
