package com.ak.logging;

import com.ak.util.LocalIO;

public enum CalibrateBuilders {
  CALIBRATION;

  public final LocalIO build(String fileName) {
    return new LogPathBuilder("json", LocalFileHandler.class).addPath(name().toLowerCase()).fileName(fileName).build();
  }
}
