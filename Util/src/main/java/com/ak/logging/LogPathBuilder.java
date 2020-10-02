package com.ak.logging;

import java.util.Optional;
import java.util.logging.FileHandler;

import javax.annotation.Nonnull;

import com.ak.util.Extension;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

class LogPathBuilder extends LocalFileIO.AbstractBuilder {
  LogPathBuilder() {
    super(Extension.NONE);
  }

  LogPathBuilder(@Nonnull Extension fileExtension, @Nonnull Class<? extends FileHandler> fileHandlerClass) {
    super(fileExtension);
    addPath(Optional
        .ofNullable(System.getProperty(fileHandlerClass.getName()))
        .orElse(fileHandlerClass.getSimpleName())
    );
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