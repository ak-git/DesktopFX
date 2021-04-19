package com.ak.logging;

import com.ak.util.Extension;
import com.ak.util.LocalIO;

public enum OutputBuilders {
  ;

  public static LocalIO build(String fileName) {
    return new OutputBuilder(Extension.CSV).fileName(fileName).build();
  }
}
