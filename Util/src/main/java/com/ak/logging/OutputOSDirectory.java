package com.ak.logging;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.ak.util.OSDirectory;
import com.ak.util.PropertiesSupport;
import com.ak.util.Strings;

import static com.ak.util.OSDirectories.USER_HOME_PATH;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  @Override
  public Path getDirectory() {
    return Arrays.stream(new String[] {"Downloads", "Documents"})
        .map(p -> Paths.get(USER_HOME_PATH).resolve(p))
        .filter(p -> Files.isDirectory(p) && Files.isWritable(p) && Files.exists(p))
        .map(p -> p.resolve(PropertiesSupport.OUT_CONVERTER_PATH.value()))
        .findFirst()
        .orElse(Paths.get(Strings.EMPTY));
  }
}
