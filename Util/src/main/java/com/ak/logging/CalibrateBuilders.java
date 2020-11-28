package com.ak.logging;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

public enum CalibrateBuilders implements Cleaner.Cleanable {
  CALIBRATION;

  @Override
  public void clean() {
    try {
      Clean.clean(newBuilder().build().getPath());
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }

  public final LocalIO build(String fileName) {
    return newBuilder().fileName(fileName).build();
  }

  private LocalFileIO.AbstractBuilder newBuilder() {
    return new LogPathBuilder(Extension.JSON, LocalFileHandler.class).addPath(name().toLowerCase());
  }
}
