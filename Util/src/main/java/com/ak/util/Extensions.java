package com.ak.util;

public enum Extensions {
  NONE {
    @Override
    public String attachTo(String fileName) {
      return fileName;
    }
  }, PROPERTIES, TXT, JSON, LOG, BIN;

  public String attachTo(String fileName) {
    return String.join(".", fileName, name().toLowerCase());
  }
}
