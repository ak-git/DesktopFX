package com.ak.logging;

import java.util.logging.FileHandler;

import javax.annotation.Nonnull;

final class LogBuilder extends LogPathBuilder {
  LogBuilder(@Nonnull Class<? extends FileHandler> fileHandlerClass) {
    super("log", fileHandlerClass);
    fileName(localDate("yyyy-MMM-dd") + ".%u.%g");
  }
}