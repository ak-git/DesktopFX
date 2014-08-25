package com.ak.storage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.LocalFileIO;

public final class LocalStorage {
  private static final LocalFileIO.AbstractBuilder BUILDER = new LocalFileIO.LocalStorageBuilder().
      addPath(LocalStorage.class.getSimpleName());

  private LocalStorage() {
    throw new AssertionError();
  }

  public static <T> void load(String fileName, Class<T> clazz, Consumer<T> consumer) {
    try (InputStream ist = BUILDER.fileName(fileName).build().openInputStream()) {
      try (XMLDecoder d = new XMLDecoder(ist)) {
        d.setExceptionListener(ex -> Logger.getLogger(LocalStorage.class.getName()).log(Level.WARNING, ex.getMessage(), ex));
        consumer.accept(clazz.cast(d.readObject()));
      }
    }
    catch (IOException e) {
      Logger.getLogger(LocalStorage.class.getName()).log(Level.CONFIG, e.getMessage(), e);
    }
  }

  public static void save(Object bean, String fileName) {
    try (ByteArrayOutputStream bst = new ByteArrayOutputStream()) {
      try (XMLEncoder e = new XMLEncoder(bst)) {
        e.setExceptionListener(ex -> Logger.getLogger(LocalStorage.class.getName()).log(Level.WARNING, ex.getMessage(), ex));
        e.writeObject(bean);
      }
      try (OutputStream out = BUILDER.fileName(fileName).build().openOutputStream()) {
        out.write(bst.toByteArray());
      }
    }
    catch (IOException e) {
      Logger.getLogger(LocalStorage.class.getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }
}
