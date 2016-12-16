package com.ak.comm.interceptor;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogLevelSubstitution {
  private LogLevelSubstitution() {
  }

  public static void substituteLogLevel(Logger logger, Level level, Runnable runnable, Consumer<LogRecord> recordConsumer) {
    Level oldLevel = logger.getLevel();
    logger.setLevel(level);
    logger.setFilter(record -> {
      if (Objects.equals(record.getLevel(), level)) {
        recordConsumer.accept(record);
      }
      return false;
    });
    runnable.run();
    logger.setFilter(null);
    logger.setLevel(oldLevel);
  }
}