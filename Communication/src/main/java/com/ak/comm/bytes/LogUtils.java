package com.ak.comm.bytes;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.ak.util.Strings.SPACE;

public enum LogUtils {
  ;

  /**
   * NmisBytesInterceptor[ 0x4b, 0xa1, 0x08, 0x2c, 0xcd, 0x14 ] 6 bytes IGNORED
   * NmisRequest[ 0x7e, 0x82, 0x08, 0x01, 0x00, 0x00, 0x00, 0x84, 0x84, 0x84, 0x84, 0x19 ] 12 bytes SEQUENCE 1 MV1 HZ_200 - 12 bytes OUT to hardware
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

  @ParametersAreNonnullByDefault
  public static String toString(Class<?> clazz, ByteBuffer buffer) {
    buffer.rewind();
    StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append("[ ");
    while (buffer.hasRemaining()) {
      sb.append("%#04x".formatted((buffer.get() & 0xFF)));
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

  @ParametersAreNonnullByDefault
  public static void logBytes(Logger logger, Level level, Object aThis, ByteBuffer buffer, String message) {
    if (logger.isLoggable(level)) {
      logger.log(level, "#%08x %s %s".formatted(aThis.hashCode(), toString(aThis.getClass(), buffer), message));
    }
  }
}
