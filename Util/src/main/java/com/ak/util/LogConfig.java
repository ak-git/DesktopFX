package com.ak.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class LogConfig {
  private LogConfig() {
    throw new AssertionError();
  }

  public static Logger newFileLogger(String applicationName, Class<?> clazz, Level level) {
    Logger logger = Logger.getLogger(clazz.getName());
    logger.setLevel(level);
    addFileHandler(applicationName, clazz, logger);
    return logger;
  }

  private static void addFileHandler(String applicationName, Class<?> clazz, Logger logger) {
    try {
      FileHandler handler = new FileHandler(String.format("%s.%%u.%%g.log",
          new LocalFileIO.LogBuilder().addPath(applicationName).fileName(clazz.getSimpleName() +
              DateTimeFormatter.ofPattern(" yyyy-MMM-dd HH-mm-ss").format(ZonedDateTime.now())).build().
              getPath().toString()), true);
      addHandler(logger, handler);
    }
    catch (Exception ex) {
      Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  private static void addHandler(Logger logger, Handler handler) {
    handler.setFormatter(new SimpleFormatter());
    handler.setLevel(logger.getLevel());
    logger.addHandler(handler);
  }
}
