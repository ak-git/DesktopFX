package com.ak.logging;

import com.ak.util.OSDirectories;
import com.ak.util.OSDirectory;

import java.nio.file.Path;

import static com.ak.util.OSDirectories.VENDOR_ID;

public enum LogOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  @Override
  public final Path getDirectory() {
    return OSDirectories.getDirectory("." + VENDOR_ID);
  }

  public static final OSDirectory DIRECTORY = OSDirectory.of(LogOSDirectory.class);
}
