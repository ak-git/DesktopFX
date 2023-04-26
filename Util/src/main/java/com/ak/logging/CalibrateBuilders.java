package com.ak.logging;

import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum CalibrateBuilders {
  ;

  private static final LocalFileIO.AbstractBuilder BUILDER = new LogPathBuilder(Extension.JSON, LocalFileHandler.class)
      .addPath(CalibrateBuilders.class.getSimpleName());

  static {
    clean();
  }

  public static void clean() {
    try {
      Clean.clean(BUILDER.build().getPath());
    }
    catch (IOException e) {
      Logger.getLogger(CalibrateBuilders.class.getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }

  public static LocalIO build(String fileName) {
    return BUILDER.fileName(fileName).build();
  }
}
