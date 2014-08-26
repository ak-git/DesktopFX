package com.ak.comm.serial;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import com.ak.util.LocalFileIO;
import gnu.io.CommPortIdentifier;

public enum ComServiceUtils {
  PORTS;

  private final Logger logger = Logger.getLogger(getClass().getName());

  ComServiceUtils() {
    try {
      FileHandler fh = new FileHandler(String.format("%s%%g.log",
          new LocalFileIO.LogBuilder().addPath("CommunicationSerial").fileName(ComServiceUtils.class.getSimpleName()).build().
              getPath().toFile().getCanonicalPath()), 256 * 1024, 4, true);
      fh.setFormatter(new SimpleFormatter());
      fh.setLevel(Level.CONFIG);
      logger.addHandler(fh);
      logger.setLevel(fh.getLevel());
    }
    catch (IOException e) {
      Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public Collection<CommPortIdentifier> getPorts() {
    if (EnableHolder.ENABLED) {
      Collection<CommPortIdentifier> identifiers = Collections.list((Enumeration<CommPortIdentifier>) CommPortIdentifier.
          getPortIdentifiers()).stream().
          filter(commPortIdentifier ->
              commPortIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL && !commPortIdentifier.isCurrentlyOwned()).
          sorted((o1, o2) -> {
            String name = o1.getName();
            return name.toLowerCase().contains("com") || name.toLowerCase().contains("serial") ? 1 : -1;
          }).collect(Collectors.toList());
      logger.config(identifiers.stream().map(CommPortIdentifier::getName).collect(Collectors.toList()).toString());
      return identifiers;
    }
    else {
      logger.info("RXTXCommDriver NOT available");
      return Collections.emptyList();
    }
  }

  private static class EnableHolder {
    private static final boolean ENABLED = isEnabled();

    private static boolean isEnabled() {
      try {
        Class.forName("gnu.io.RXTXCommDriver");
        return true;
      }
      catch (ClassNotFoundException e) {
        Logger.getLogger(ComServiceUtils.class.getName()).log(Level.FINE, e.getMessage(), e);
      }
      return false;
    }
  }
}
