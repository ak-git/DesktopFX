package com.ak.storage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

public final class LocalStorage<T> extends AbstractStorage<T> {
  private static final LocalFileIO.AbstractBuilder BUILDER = new LocalStorageBuilder().addPath(LocalStorage.class.getSimpleName());

  @Nonnull
  private final String fileSuffix;
  @Nonnull
  private final Class<T> clazz;
  @Nullable
  private T t;

  public LocalStorage(@Nonnull String filePrefix, @Nonnull String fileSuffix, @Nonnull Class<T> clazz) {
    super(filePrefix);
    this.fileSuffix = fileSuffix;
    this.clazz = clazz;
  }

  @Override
  public void save(@Nonnull T t) {
    this.t = t;
    save(t, fileName());
  }

  @Override
  public void update(@Nullable T t) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public T get() {
    if (t == null) {
      load(fileName(), clazz, value -> t = value);
    }
    return t;
  }

  @Override
  public void delete() {
    try {
      Files.deleteIfExists(BUILDER.fileName(fileName()).build().getPath());
    }
    catch (IOException e) {
      warning(e);
    }
  }

  private String fileName() {
    return String.format("%s_%s", getFilePrefix(), fileSuffix);
  }

  private static <T> void load(@Nonnull String fileName, @Nonnull Class<? extends T> clazz, @Nonnull Consumer<? super T> consumer) {
    LocalIO localIO = BUILDER.fileName(fileName).build();
    try {
      if (Files.exists(localIO.getPath(), LinkOption.NOFOLLOW_LINKS)) {
        try (InputStream ist = localIO.openInputStream()) {
          try (XMLDecoder d = new XMLDecoder(ist)) {
            d.setExceptionListener(LocalStorage::warning);
            consumer.accept(clazz.cast(d.readObject()));
          }
        }
      }
    }
    catch (IOException e) {
      warning(e);
    }
  }

  private static void save(@Nonnull Object bean, @Nonnull String fileName) {
    try (ByteArrayOutputStream bst = new ByteArrayOutputStream()) {
      try (XMLEncoder e = new XMLEncoder(bst)) {
        e.setExceptionListener(LocalStorage::warning);
        e.writeObject(bean);
      }
      try (OutputStream out = BUILDER.fileName(fileName).build().openOutputStream()) {
        out.write(bst.toByteArray());
      }
    }
    catch (IOException e) {
      warning(e);
    }
  }

  private static void warning(@Nonnull Exception ex) {
    Logger.getLogger(LocalStorage.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
  }
}
