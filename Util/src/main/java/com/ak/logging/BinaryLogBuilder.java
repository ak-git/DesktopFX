package com.ak.logging;

import com.ak.util.LocalIO;
import com.ak.util.Strings;

public enum BinaryLogBuilder {
  SIMPLE(Strings.EMPTY) {
    @Override
    public LocalIO build(String fileName) {
      return newInstance().fileName(fileName).build();
    }
  },
  TIME(Strings.EMPTY) {
    @Override
    public LocalIO build(String fileName) {
      return newInstance().fileNameWithTime(fileName).build();
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

  BinaryLogBuilder(String directory) {
    this.directory = directory;
  }

  public LocalIO build(String fileName) {
    return newInstance().fileNameWithTime(fileName).addPath(directory).build();
  }

  private static LogPathBuilder newInstance() {
    return new LogPathBuilder("bin", LocalFileHandler.class);
  }
}
