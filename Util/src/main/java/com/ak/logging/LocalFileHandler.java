package com.ak.logging;

import java.io.IOException;
import java.util.logging.FileHandler;

public final class LocalFileHandler extends FileHandler {
  public LocalFileHandler() throws IOException {
    super(new LogBuilder(LocalFileHandler.class).build().getPath().toString(), true);
  }
}