package com.ak.util;

import java.io.IOException;
import java.util.logging.FileHandler;

public final class LocalFileHandler extends FileHandler {
  public LocalFileHandler() throws IOException, SecurityException {
    super(new LocalFileIO.LogBuilder(LocalFileHandler.class).build().getPath().toString(), true);
  }
}