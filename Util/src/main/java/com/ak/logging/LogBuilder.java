package com.ak.logging;

import com.ak.util.Extension;
import com.ak.util.LocalFileIO;

import java.util.logging.FileHandler;

final class LogBuilder extends LogPathBuilder {
  LogBuilder(Class<? extends FileHandler> fileHandlerClass) {
    super(Extension.LOG, fileHandlerClass);
    fileName(LocalFileIO.AbstractBuilder.localDate("yyyy-MM-dd") + ".%u.%g");
  }
}