package com.ak.comm.serial;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import gnu.io.CommPortIdentifier;

public enum ComServiceUtils {
  PORTS;

  private static final Logger LOGGER = Logger.getLogger(ComServiceUtils.class.getName());
  private final LinkedList<String> usedPorts = new LinkedList<>();

  @SuppressWarnings("unchecked")
  public CommPortIdentifier next(String preferredName) {
    if (EnableHolder.ENABLED) {
      List<CommPortIdentifier> identifiers = Collections.list((Enumeration<CommPortIdentifier>) CommPortIdentifier.
          getPortIdentifiers()).stream().
          filter(commPortIdentifier ->
              commPortIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL && !commPortIdentifier.isCurrentlyOwned()).
          sorted((o1, o2) -> usedPorts.indexOf(o1.getName()) - usedPorts.indexOf(o2.getName())).
          collect(Collectors.toList());

      CommPortIdentifier portIdentifier = identifiers.stream().
          filter(commPortIdentifier -> commPortIdentifier.getName().equals(preferredName)).findFirst().
          orElse(identifiers.iterator().next());
      LOGGER.config(String.format("%s, the '%s' is selected",
          identifiers.stream().map(CommPortIdentifier::getName).collect(Collectors.toList()).toString(),
          portIdentifier.getName()));
      usedPorts.remove(portIdentifier.getName());
      usedPorts.addLast(portIdentifier.getName());
      return portIdentifier;
    }
    else {
      LOGGER.info("RXTXCommDriver NOT available");
      return null;
    }
  }

  private static class EnableHolder {
    private static final boolean ENABLED = isEnabled();

    @SuppressWarnings("ErrorNotRethrown")
    private static boolean isEnabled() {
      try {
        Class.forName("gnu.io.RXTXCommDriver");
        return true;
      }
      catch (ClassNotFoundException | LinkageError e) {
        LOGGER.log(Level.FINE, e.getMessage(), e);
      }
      return false;
    }
  }
}
