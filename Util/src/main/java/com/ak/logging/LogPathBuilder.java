package com.ak.logging;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

public class LogPathBuilder extends LocalFileIO.AbstractBuilder {
  public LogPathBuilder() {
    super("");
  }

  LogPathBuilder(String fileExtension, Class<? extends FileHandler> fileHandlerClass) {
    super(fileExtension);
    addPath(Optional.ofNullable(LogManager.getLogManager().getProperty(fileHandlerClass.getName() + ".name")).
        orElse(fileHandlerClass.getSimpleName()));
  }

  static String localDate(String pattern) {
    return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.now());
  }

  /**
   * Open file (for <b>background logging</b>) in directory
   * <ul>
   * <li>
   * Windows - ${userHome}/Application Data/${vendorId}/${applicationId}
   * </li>
   * <li>
   * MacOS - ${userHome}/Library/Application Support/${vendorId}/${applicationId}
   * </li>
   * <li>
   * Unix and other - ${userHome}/.${applicationId}
   * </li>
   * </ul>
   *
   * @return interface for input/output file creation.
   */
  @Override
  public final LocalIO build() {
    return new LocalFileIO<>(this, LogOSDirectory.class);
  }
}