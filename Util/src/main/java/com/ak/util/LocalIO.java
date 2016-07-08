package com.ak.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import javax.annotation.Nonnull;

public interface LocalIO {
  @Nonnull
  Path getPath() throws IOException;

  @Nonnull
  InputStream openInputStream() throws IOException;

  @Nonnull
  OutputStream openOutputStream() throws IOException;
}
