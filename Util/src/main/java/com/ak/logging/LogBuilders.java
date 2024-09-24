package com.ak.logging;

import com.ak.util.*;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum LogBuilders implements Cleaner.Cleanable {
  SIMPLE(Strings.EMPTY) {
    @Override
    public LocalIO build(String fileName) {
      return newInstance().fileName(fileName).build();
    }
  },
  TIME(Strings.EMPTY) {
    @Override
    public LocalIO build(String fileName) {
      return newInstance().fileNameWithDateTime(fileName).build();
    }
  },
  SERIAL_BYTES("serialBytesLog"),
  CONVERTER_SERIAL("converterSerialLog"),
  CONVERTER_FILE("converterFileLog") {
    @Override
    public void clean() {
      try {
        Clean.clean(newBuilder().build().getPath());
      }
      catch (IOException e) {
        LOGGER.log(Level.WARNING, e.getMessage(), e);
      }
    }

    @Override
    public LocalIO build(String fileName) {
      return newBuilder().fileName(fileName).build();
    }

    private static LocalFileIO.AbstractBuilder newBuilder() {
      return newInstance().addPath(CONVERTER_FILE.directory);
    }
  };

  private static final Logger LOGGER = Logger.getLogger(LogBuilders.class.getName());
  private final String directory;

  LogBuilders(String directory) {
    this.directory = directory;
  }

  public LocalIO build(String fileName) {
    return newInstance().fileNameWithDateTime(fileName).addPath(directory).addPathWithDate().build();
  }

  @Override
  public void clean() {
    LOGGER.finest(() -> "Clean ignore: %s".formatted(directory));
  }

  private static LogPathBuilder newInstance() {
    return new LogPathBuilder(Extension.BIN, LocalFileHandler.class);
  }
}
