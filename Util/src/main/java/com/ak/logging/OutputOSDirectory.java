package com.ak.logging;

import com.ak.util.OSDirectories;
import com.ak.util.OSDirectory;

import java.nio.file.Path;

import static com.ak.util.OSDirectories.VENDOR_ID;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  public static final OSDirectory DIRECTORY = OSDirectory.of(OutputOSDirectory.class);
  private static final String[] CANDIDATES = {"Downloads", "Documents"};

  @Override
  public Path getDirectory() {
    return OSDirectories.getDirectory(CANDIDATES).resolve(VENDOR_ID);
  }
}
