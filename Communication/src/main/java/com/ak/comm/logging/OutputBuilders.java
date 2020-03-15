package com.ak.comm.logging;

import com.ak.logging.OutputBuilder;
import com.ak.util.Extensions;
import com.ak.util.LocalIO;

public enum OutputBuilders {
  ;

  public static LocalIO build(String fileName) {
    return new OutputBuilder(Extensions.TXT).addPathWithDate().fileNameWithDateTime(fileName).build();
  }
}
