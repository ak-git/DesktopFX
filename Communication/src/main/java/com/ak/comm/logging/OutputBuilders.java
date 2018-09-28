package com.ak.comm.logging;

import com.ak.logging.OutputBuilder;
import com.ak.util.LocalIO;

public enum OutputBuilders {
  ;

  public static LocalIO build(String fileName) {
    return new OutputBuilder("txt").addPathWithDate().fileNameWithDateTime(fileName).build();
  }
}