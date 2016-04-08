package com.ak.logging;

import java.util.logging.FileHandler;

final class LogBuilder extends LogPathBuilder {
  LogBuilder(Class<? extends FileHandler> fileHandlerClass) {
    super("log", fileHandlerClass);
    fileName(localDate("yyyy-MMM-dd") + ".%u.%g");
  }
}