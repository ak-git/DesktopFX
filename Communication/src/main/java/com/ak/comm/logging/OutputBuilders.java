package com.ak.comm.logging;

import com.ak.logging.OutputBuilder;
import com.ak.util.LocalIO;

public enum OutputBuilders {
  TIME {
    @Override
    public LocalIO build(String fileName) {
      return newInstance().fileNameWithTime(fileName).build();
    }
  };

  public LocalIO build(String fileName) {
    return newInstance().fileNameWithTime(fileName).build();
  }

  private static OutputBuilder newInstance() {
    return new OutputBuilder("txt");
  }
}
