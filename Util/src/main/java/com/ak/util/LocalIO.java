package com.ak.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface LocalIO {
  Path getPath() throws IOException;

  InputStream openInputStream() throws IOException;
}
