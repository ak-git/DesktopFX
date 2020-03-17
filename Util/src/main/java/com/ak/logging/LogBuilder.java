package com.ak.logging;

import java.util.logging.FileHandler;

import javax.annotation.Nonnull;

import com.ak.util.Extension;
import com.ak.util.LocalFileIO;

final class LogBuilder extends LogPathBuilder {
  LogBuilder(@Nonnull Class<? extends FileHandler> fileHandlerClass) {
    super(Extension.LOG, fileHandlerClass);
    fileName(LocalFileIO.AbstractBuilder.localDate("yyyy-MM-dd") + ".%u.%g");
  }
}