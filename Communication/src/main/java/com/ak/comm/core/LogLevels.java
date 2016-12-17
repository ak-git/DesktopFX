package com.ak.comm.core;

import java.util.logging.Level;

public enum LogLevels {
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
}
