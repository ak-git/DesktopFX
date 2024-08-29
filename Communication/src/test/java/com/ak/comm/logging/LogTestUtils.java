package com.ak.comm.logging;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogTestUtils {
  private LogTestUtils() {
  }

  public static boolean isSubstituteLogLevel(Logger logger, Level level, Runnable runnable, Consumer<LogRecord> recordConsumer) {
    Level oldLevel = logger.getLevel();
    logger.setLevel(level);
    AtomicBoolean okFlag = new AtomicBoolean();
    logger.setFilter(r -> {
      if (Objects.equals(r.getLevel(), level)) {
        recordConsumer.accept(r);
        okFlag.set(true);
      }
      return false;
    });
    runnable.run();
    logger.setFilter(null);
    logger.setLevel(oldLevel);
    return okFlag.get();
  }
}
