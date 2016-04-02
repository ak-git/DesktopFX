package com.ak.util;

import java.io.IOException;
import java.util.logging.FileHandler;

public final class LocalFileHandler extends FileHandler {
  public LocalFileHandler() throws IOException, SecurityException {
    super(String.format("%s.%%u.%%g.log",
        new LocalFileIO.LogBuilder().addPathAndDate(LocalFileHandler.class).build().getPath().toString()), true);
  }
}