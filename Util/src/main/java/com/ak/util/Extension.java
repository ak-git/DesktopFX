package com.ak.util;

import javax.annotation.Nonnull;

public enum Extension {
  NONE {
    @Override
    public String attachTo(String fileName) {
      return fileName;
    }
  }, PROPERTIES, TXT, JSON, LOG, BIN, CSV;

  public String attachTo(@Nonnull String fileName) {
    return String.join(".", fileName, name().toLowerCase());
  }

  public final String clean(@Nonnull String fileName) {
    String lowCase = fileName.toLowerCase();
    String remove = attachTo(Strings.EMPTY);
    if (lowCase.endsWith(remove)) {
      return fileName.substring(0, lowCase.lastIndexOf(remove));
    }
    else {
      return fileName;
    }
  }

  public final boolean is(@Nonnull String fileName) {
    return fileName.toLowerCase().endsWith(attachTo(Strings.EMPTY));
  }
}
