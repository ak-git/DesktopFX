package com.ak.logging;

import com.ak.util.Extension;

import java.util.logging.FileHandler;

final class LogBuilder extends LogPathBuilder {
  LogBuilder(Class<? extends FileHandler> fileHandlerClass) {
    super(Extension.LOG, fileHandlerClass);
    fileName(localDate("yyyy-MM-dd") + ".%u.%g");
  }
}