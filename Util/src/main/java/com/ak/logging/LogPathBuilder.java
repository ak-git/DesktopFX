package com.ak.logging;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

import javax.annotation.Nonnull;

import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;
import com.ak.util.Strings;

public class LogPathBuilder extends LocalFileIO.AbstractBuilder {
  public LogPathBuilder() {
    super(Strings.EMPTY);
  }

  LogPathBuilder(@Nonnull String fileExtension, @Nonnull Class<? extends FileHandler> fileHandlerClass) {
    super(fileExtension);
    addPath(Optional.ofNullable(LogManager.getLogManager().getProperty(fileHandlerClass.getName() + ".name")).
        orElse(fileHandlerClass.getSimpleName()));
  }

  @Nonnull
  static String localDate(@Nonnull String pattern) {
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
  @Nonnull
  @Override
  public final LocalIO build() {
    return new LocalFileIO<>(this, LogOSDirectory.class);
  }
}