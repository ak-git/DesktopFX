package com.ak.util;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalFileIO implements LocalIO {
  private final Path path;
  private final String fileName;

  public LocalFileIO(AbstractBuilder b, OSDirectory directory) {
    Path p = b.relativePath == null ? Paths.get(Strings.EMPTY) : b.relativePath;
    path = directory.getDirectory().resolve(p);
    fileName = b.fileName.strip();
  }

  @Override
  public Path getPath() throws IOException {
    Files.createDirectories(path);
    Path p = path;
    if (!fileName.isEmpty()) {
      p = p.resolve(fileName);
    }
    return p;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return Files.newInputStream(getPath());
  }

  public abstract static class AbstractBuilder implements Builder<LocalIO> {
    private final Extension fileExtension;
    private @Nullable Path relativePath;
    private String fileName = Strings.EMPTY;

    protected AbstractBuilder(Extension fileExtension) {
      this.fileExtension = Objects.requireNonNull(fileExtension);
    }

    public final AbstractBuilder addPath(String part) {
      if (relativePath == null) {
        relativePath = Paths.get(part);
      }
      else {
        relativePath = relativePath.resolve(part);
      }
      return this;
    }

    public final AbstractBuilder addPathWithDate() {
      return addPath(localDate("yyyy-MM-dd"));
    }

    public final AbstractBuilder fileName(String fileName) {
      this.fileName = fileExtension.attachTo(fileName);
      return this;
    }

    public final AbstractBuilder fileNameWithDateTime(String suffix) {
      fileName("%s %s".formatted(localDate("yyyy-MM-dd HH-mm-ss SSS"), Objects.requireNonNull(suffix)));
      return this;
    }

    public static String localDate(String pattern) {
      return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.now());
    }
  }
}
