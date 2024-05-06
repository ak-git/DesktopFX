package com.ak.logging;

import com.ak.util.Extension;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

public final class OutputBuilder extends LocalFileIO.AbstractBuilder {
  public OutputBuilder(Extension fileExtension) {
    super(fileExtension);
  }

  /**
   * Open file (for <b>output saving</b>) in directory ${userHome}/Downloads/${applicationId}
   *
   * @return interface for input/output file creation.
   */
  @Override
  public LocalIO build() {
    return new LocalFileIO(this, OutputOSDirectory.Constants.DIRECTORY);
  }
}

