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

import com.ak.util.LocalFileIO;
import com.ak.util.LocalIO;

public final class LocalStorage<T> extends AbstractStorage<T> {
  private static final LocalFileIO.AbstractBuilder BUILDER = new LocalStorageBuilder().addPath(LocalStorage.class.getSimpleName());

  private final String fileSuffix;
  private final Class<T> clazz;
  private T t;

  public LocalStorage(String filePrefix, String fileSuffix, Class<T> clazz) {
    super(filePrefix);
    this.fileSuffix = fileSuffix;
    this.clazz = clazz;
  }

  @Override
  public void save(T t) {
    this.t = t;
    save(t, fileName());
  }

  @Override
  public void update(T t) {
    throw new UnsupportedOperationException();
  }

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

  private static <T> void load(String fileName, Class<? extends T> clazz, Consumer<? super T> consumer) {
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

  private static void save(Object bean, String fileName) {
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

  private static void warning(Exception ex) {
    Logger.getLogger(LocalStorage.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
  }
}
