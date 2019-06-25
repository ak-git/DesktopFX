package com.ak.logging;

import com.ak.util.LocalIO;
import com.ak.util.Strings;

public enum LogBuilders {
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
  SERIAL_BYTES("serialBytesLog"), CONVERTER_SERIAL("converterSerialLog"),
  CONVERTER_FILE("converterFileLog") {
    @Override
    public LocalIO build(String fileName) {
      return newInstance().fileName(fileName).addPath(CONVERTER_FILE.directory).build();
    }
  };

  private final String directory;

  LogBuilders(String directory) {
    this.directory = directory;
  }

  public LocalIO build(String fileName) {
    return newInstance().fileNameWithDateTime(fileName).addPath(directory).addPathWithDate().build();
  }

  private static LogPathBuilder newInstance() {
    return new LogPathBuilder("bin", LocalFileHandler.class);
  }
}