package com.ak.storage;

import javax.annotation.Nonnull;

import com.ak.logging.LogOSDirectory;
import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

final class LocalStorageBuilder extends LocalFileIO.AbstractBuilder {
  LocalStorageBuilder() {
    super("xml");
  }

  @Nonnull
  @Override
  public LocalIO build() {
    return new LocalFileIO<>(this, LogOSDirectory.class);
  }
}
