package com.ak.logging;

import javax.annotation.Nonnull;

import com.ak.util.Extension;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

public final class OutputBuilder extends LocalFileIO.AbstractBuilder {
  public OutputBuilder(@Nonnull Extension fileExtension) {
    super(fileExtension);
  }

  /**
   * Open file (for <b>output saving</b>) in directory ${userHome}/Downloads/${applicationId}
   *
   * @return interface for input/output file creation.
   */
  @Override
  public LocalIO build() {
    return new LocalFileIO<>(this, OutputOSDirectory.class);
  }
}

