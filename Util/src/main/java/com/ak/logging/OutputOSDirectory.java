package com.ak.logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ak.util.OSDirectory;
import com.ak.util.PropertiesSupport;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  @Override
  public Path getDirectory() {
    String[] paths = {"/Downloads/", "/Documents/"};

    Path result = Paths.get(USER_HOME_PATH);
    for (String path : paths) {
      File file = new File(USER_HOME_PATH, path);
      if (file.exists() && file.isDirectory() && !file.isHidden()) {
        result = Paths.get(USER_HOME_PATH, path, PropertiesSupport.OUT_CONVERTER_PATH.value());
        break;
      }
    }
    return result;
  }
}
