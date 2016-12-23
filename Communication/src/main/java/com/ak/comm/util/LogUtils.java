package com.ak.comm.util;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import static com.ak.util.Strings.SPACE;

public enum LogUtils {
  ;

  /**
   * NmisBytesInterceptor[ 0x4b, 0xa1, 0x08, 0x2c, 0xcd, 0x14 ] 6 bytes IGNORED
   */
  public static final Level LOG_LEVEL_ERRORS = Level.CONFIG;
  /**
   * [ R1 = 2850, R2 = 2985 ]
   */
  public static final Level LOG_LEVEL_VALUES = Level.FINE;
  /**
   * RsceCommandFrame[ 0x00, 0x09, 0xc7, 0x22, 0x0b, 0xa9, 0x0b, 0x64, 0x00, 0xa5, 0x8b ] 11 bytes ALL NONE RESERVE
   */
  public static final Level LOG_LEVEL_LEXEMES = Level.FINER;
  /**
   * [ 0x09, 0x04, 0x68, 0x2a, 0xda, 0xfe ] 6 bytes IN from hardware
   */
  public static final Level LOG_LEVEL_BYTES = Level.FINEST;

  public static String toString(@Nonnull Class<?> clazz, @Nonnull ByteBuffer buffer) {
    buffer.rewind();
    StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[ ");
    while (buffer.hasRemaining()) {
      sb.append(String.format("%#04x", (buffer.get() & 0xFF)));
      if (buffer.hasRemaining()) {
        sb.append(',');
      }
      sb.append(SPACE);
    }
    sb.append("]");
    if (buffer.limit() > 1) {
      sb.append(SPACE).append(buffer.limit()).append(" bytes");
    }
    buffer.rewind();
    return sb.toString();
  }

  public static void logBytes(@Nonnull Logger logger, @Nonnull Level level, @Nonnull Object aThis, @Nonnull ByteBuffer buffer,
                              @Nonnull String message) {
    if (logger.isLoggable(level)) {
      logger.log(level, String.format("#%x %s %s", aThis.hashCode(), toString(aThis.getClass(), buffer), message));
    }
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
