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
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

import javafx.util.Builder;

public class LocalFileIO<E extends Enum<E> & OSDirectory> implements LocalIO {
  private final Path path;
  private final String fileName;
  private final E osIdEnum;

  private LocalFileIO(AbstractBuilder b, Class<E> enumClass) {
    path = b.relativePath;
    fileName = Optional.ofNullable(b.fileName).orElse("");
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
    private final String fileExtension;
    private Path relativePath;
    private String fileName;

    AbstractBuilder(String fileExtension) {
      this.fileExtension = fileExtension;
    }

    public final AbstractBuilder addPath(String part) {
      if (relativePath == null) {
        relativePath = Paths.get(part);
      }
      else {
        relativePath.resolve(part);
      }
      return this;
    }

    public final AbstractBuilder fileName(String fileName) {
      this.fileName = fileName;
      if (!fileExtension.isEmpty()) {
        this.fileName += "." + fileExtension;
      }
      return this;
    }
  }

  public static class LogPathBuilder extends AbstractBuilder {
    public LogPathBuilder() {
      super("");
    }

    private LogPathBuilder(String fileExtension, Class<? extends FileHandler> fileHandlerClass) {
      super(fileExtension);
      addPath(Optional.ofNullable(LogManager.getLogManager().getProperty(fileHandlerClass.getName() + ".name")).
          orElse(fileHandlerClass.getSimpleName()));
    }

    static String localDate(String pattern) {
      return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.now());
    }

    /**
     * Open file (for <b>background logging</b>) in directory
     * <ul>
     * <li>
     * Windows - ${userHome}/Application Data/${vendorId}/${applicationId}
     * </li>
     * <li>
     * MacOS - ${userHome}/Library/Application Support/${vendorId}/${applicationId}
     * </li>
     * <li>
     * Unix and other - ${userHome}/.${applicationId}
     * </li>
     * </ul>
     *
     * @return interface for input/output file creation.
     */
    @Override
    public final LocalIO build() {
      return new LocalFileIO<>(this, LogOSDirectory.class);
    }
  }

  static final class LogBuilder extends LogPathBuilder {
    LogBuilder(Class<? extends FileHandler> fileHandlerClass) {
      super("log", fileHandlerClass);
      fileName(localDate("yyyy-MMM-dd") + ".%u.%g");
    }
  }

  public static final class BinaryLogBuilder extends LogPathBuilder {
    public BinaryLogBuilder(String prefix, Class<? extends FileHandler> fileHandlerClass) {
      super("bin", fileHandlerClass);
      fileName(prefix + localDate(" yyyy-MMM-dd HH-mm-ss"));
    }
  }

  public static final class LocalStorageBuilder extends AbstractBuilder {
    public LocalStorageBuilder() {
      super("xml");
    }

    @Override
    public LocalIO build() {
      return new LocalFileIO<>(this, LogOSDirectory.class);
    }
  }
}
