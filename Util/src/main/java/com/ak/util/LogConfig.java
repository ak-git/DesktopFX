package com.ak.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class LogConfig {
  private enum LogMapping {
    OFF(Level.OFF, org.apache.log4j.Level.OFF),
    SEVERE(Level.SEVERE, org.apache.log4j.Level.FATAL),
    WARNING(Level.WARNING, org.apache.log4j.Level.ERROR),
    INFO(Level.INFO, org.apache.log4j.Level.WARN),
    CONFIG(Level.CONFIG, org.apache.log4j.Level.INFO),
    FINE(Level.FINE, org.apache.log4j.Level.DEBUG),
    FINER(Level.FINER, org.apache.log4j.Level.TRACE),
    FINEST(Level.FINEST, org.apache.log4j.Level.ALL),
    ALL(Level.ALL, org.apache.log4j.Level.ALL);

    private final Level level;
    private final org.apache.log4j.Level levelLog4j;

    LogMapping(Level level, org.apache.log4j.Level levelLog4j) {
      this.level = level;
      this.levelLog4j = levelLog4j;
    }

    static Level find(org.apache.log4j.Level levelLog4j) {
      for (LogMapping logMapping : values()) {
        if (logMapping.levelLog4j.equals(levelLog4j)) {
          return logMapping.level;
        }
      }
      throw new IllegalArgumentException(levelLog4j.toString());
    }
  }

  private LogConfig() {
    throw new AssertionError();
  }

  public static void initLogger(String applicationName, Runnable runnable) {
    LogMapping logMapping = LogMapping.valueOf(PropertiesSupport.LOG_LEVEL.get());
    final Logger logger = Logger.getLogger(LocalFileIO.VENDOR_ID);
    logger.setUseParentHandlers(false);
    logger.setLevel(logMapping.level);

    BasicConfigurator.configure(new AppenderSkeleton() {
      private final Layout patternLayout = new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN);

      @Override
      protected void append(LoggingEvent event) {
        ThrowableInformation information = event.getThrowableInformation();
        if (information == null) {
          logger.log(LogMapping.find(event.getLevel()), patternLayout.format(event).trim(), event.getMessage());
        }
        else {
          logger.log(LogMapping.find(event.getLevel()), patternLayout.format(event).trim(), information.getThrowable());
        }
      }

      @Override
      public void close() {
      }

      @Override
      public boolean requiresLayout() {
        return true;
      }
    });
    org.apache.log4j.Logger.getRootLogger().setLevel(logMapping.levelLog4j);

    newFileHandler(applicationName, LogConfig.class, logger);
    addHandler(logger, new ConsoleHandler());
    try {
      runnable.run();
    }
    catch (RuntimeException e) {
      logger.log(Level.SEVERE, "RuntimeException during startup", e);
    }
  }

  public static void newFileHandler(String applicationName, Class<?> clazz, Logger logger) {
    try {
      FileHandler handler = new FileHandler(String.format("%s.%%u.%%g.log",
          new LocalFileIO.LogBuilder().addPath(applicationName).fileName(clazz.getSimpleName()).build().
              getPath().toFile().getCanonicalPath()), 256 * 1024, 4, true);
      addHandler(logger, handler);
      logger.log(logger.getLevel(), "Application starting up\n");
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
