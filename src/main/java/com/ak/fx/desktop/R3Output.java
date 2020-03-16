package com.ak.fx.desktop;

import org.beryx.textio.TextIO;
import org.beryx.textio.system.SystemTextTerminal;

public final class R3Output {
  private R3Output() {
  }

  public static void main(String[] args) {
    SystemTextTerminal terminal = new SystemTextTerminal();
    TextIO textIO = new TextIO(terminal);
    terminal.println("################");
    terminal.println("# Layer 3 INFO #");
    terminal.println("################");
    terminal.println();
    terminal.setBookmark("MAIN");
    while (!Thread.currentThread().isInterrupted()) {

      if (textIO.newBooleanInputReader()
          .withPropertiesPrefix("exit")
          .withDefaultValue(true).read("Run again?")) {
        terminal.resetToBookmark("MAIN");
      }
      else {
        break;
      }
    }
    textIO.dispose();
  }
}
