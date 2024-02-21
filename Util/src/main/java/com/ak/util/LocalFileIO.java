package com.ak.util;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class LocalFileIO<E extends Enum<E> & OSDirectory> implements LocalIO {
  private final Path path;
  private final String fileName;
  private final E osIdEnum;

  public LocalFileIO(AbstractBuilder b, Class<E> enumClass) {
    path = b.relativePath == null ? Paths.get(Strings.EMPTY) : b.relativePath;
    fileName = Optional.ofNullable(b.fileName).orElse(Strings.EMPTY).trim();
    osIdEnum = Enum.valueOf(enumClass, OS.get().name());
  }

  @Override
  public Path getPath() throws IOException {
    Path p = osIdEnum.getDirectory().resolve(path);
    Files.createDirectories(p);
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
    private Path relativePath;
    @Nullable
    private String fileName;

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
      fileName(localDate("yyyy-MM-dd HH-mm-ss SSS ") + Objects.requireNonNull(suffix));
      return this;
    }

    public static String localDate(String pattern) {
      return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.now());
    }
  }
}
