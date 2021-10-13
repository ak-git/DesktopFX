package com.ak.logging;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.Clean;
import com.ak.util.Extension;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;
import com.ak.util.Strings;

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
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
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

  private final String directory;

  LogBuilders(String directory) {
    this.directory = directory;
  }

  public LocalIO build(String fileName) {
    return newInstance().fileNameWithDateTime(fileName).addPath(directory).addPathWithDate().build();
  }

  @Override
  public void clean() {
    throw new UnsupportedOperationException(name());
  }

  private static LogPathBuilder newInstance() {
    return new LogPathBuilder(Extension.BIN, LocalFileHandler.class);
  }
}
