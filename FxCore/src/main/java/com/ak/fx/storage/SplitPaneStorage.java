package com.ak.fx.storage;

import javafx.scene.control.SplitPane;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.prefs.BackingStoreException;

public final class SplitPaneStorage implements Storage<SplitPane> {
  @Nonnull
  private final DoubleArrayStorage dividerStorage;
  private boolean saveReady = false;

  public SplitPaneStorage(@Nonnull Class<?> c, @Nonnull String nodeName) {
    dividerStorage = new DoubleArrayStorage(c, nodeName);
  }

  @Override
  public void save(@Nonnull SplitPane splitPane) {
    if (saveReady) {
      dividerStorage.save(splitPane.getDividers().stream().mapToDouble(SplitPane.Divider::getPosition).toArray());
    }
  }

  @Override
  public void update(@Nonnull SplitPane splitPane) {
    double[] array = dividerStorage.get();
    if (array.length == splitPane.getDividers().size()) {
      splitPane.setDividerPositions(array);
    }
    saveReady = true;
  }

  @Nullable
  @Override
  public SplitPane get() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() throws BackingStoreException {
    dividerStorage.delete();
  }
}
