package com.ak.logging;

import java.nio.file.Path;

import com.ak.util.OSDirectories;
import com.ak.util.OSDirectory;
import com.ak.util.PropertiesSupport;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  private static final String[] CANDIDATES = {"Downloads", "Documents"};

  @Override
  public Path getDirectory() {
    return OSDirectories.getDirectory(CANDIDATES).resolve(PropertiesSupport.OUT_CONVERTER_PATH.value());
  }
}
