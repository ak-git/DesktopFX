package com.ak.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.util.Builder;

public class LocalFileIO<E extends Enum<E> & OSDirectory> implements LocalIO {
  @Nonnull
  private final Path path;
  @Nonnull
  private final String fileName;
  @Nonnull
  private final E osIdEnum;

  public LocalFileIO(@Nonnull AbstractBuilder b, @Nonnull Class<E> enumClass) {
    path = b.relativePath == null ? Paths.get(Strings.EMPTY) : b.relativePath;
    fileName = Optional.ofNullable(b.fileName).orElse(Strings.EMPTY).trim();
    osIdEnum = Enum.valueOf(enumClass, OS.get().name());
  }

  @Override
  public Path getPath() throws IOException {
    Path path = osIdEnum.getDirectory().resolve(this.path);
    Files.createDirectories(path);
    if (!fileName.isEmpty()) {
      path = path.resolve(fileName);
    }
    return path;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return Files.newInputStream(getPath());
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    return Files.newOutputStream(getPath());
  }

  public abstract static class AbstractBuilder implements Builder<LocalIO> {
    @Nonnull
    private final String fileExtension;
    private Path relativePath;
    @Nullable
    private String fileName;

    public AbstractBuilder(@Nonnull String fileExtension) {
      this.fileExtension = fileExtension;
    }

    public final AbstractBuilder addPath(@Nonnull String part) {
      if (relativePath == null) {
        relativePath = Paths.get(part);
      }
      else {
        relativePath = relativePath.resolve(part);
      }
      return this;
    }

    public final AbstractBuilder fileName(@Nonnull String fileName) {
      this.fileName = fileName;
      if (!fileExtension.isEmpty()) {
        this.fileName += "." + fileExtension;
      }
      return this;
    }

    public final AbstractBuilder fileNameWithTime(@Nonnull String prefix) {
      fileName(prefix + localDate(" yyyy-MMM-dd HH-mm-ss"));
      return this;
    }

    public static String localDate(@Nonnull String pattern) {
      return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.now());
    }
  }
}
