package com.ak.comm.log;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

public class LogTestUtils {
  private LogTestUtils() {
  }

  @ParametersAreNonnullByDefault
  public static boolean isSubstituteLogLevel(Logger logger, Level level, Runnable runnable, Consumer<LogRecord> recordConsumer) {
    Level oldLevel = logger.getLevel();
    logger.setLevel(level);
    AtomicBoolean okFlag = new AtomicBoolean();
    logger.setFilter(record -> {
      if (Objects.equals(record.getLevel(), level)) {
        recordConsumer.accept(record);
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
