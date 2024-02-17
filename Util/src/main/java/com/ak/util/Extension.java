package com.ak.util;

public enum Extension {
  NONE {
    @Override
    public String attachTo(String fileName) {
      return fileName;
    }
  }, PROPERTIES, TXT, JSON, LOG, BIN, CSV;

  public String attachTo(String fileName) {
    if (fileName.endsWith(".%s".formatted(name().toLowerCase()))) {
      return fileName;
    }
    else {
      return String.join(".", fileName, name().toLowerCase());
    }
  }

  public final String clean(String fileName) {
    String lowCase = fileName.toLowerCase();
    String remove = attachTo(Strings.EMPTY);
    if (lowCase.endsWith(remove)) {
      return fileName.substring(0, lowCase.lastIndexOf(remove));
    }
    else {
      return fileName;
    }
  }

  public final boolean is(String fileName) {
    return fileName.toLowerCase().endsWith(attachTo(Strings.EMPTY));
  }
}
