package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import javax.annotation.Nonnull;

import javafx.stage.Screen;
import javafx.stage.Stage;

abstract class AbstractStageStorage extends AbstractStorage<Stage> {
  private static final String FULL_SCREEN = "fullScreen";
  private static final String MAXIMIZED = "maximized";
  @Nonnull
  private final Storage<Rectangle2D.Double> boundsStorage;

  AbstractStageStorage(@Nonnull Class<?> c, @Nonnull String nodeName) {
    super(c, nodeName);
    boundsStorage = new BoundsStorage(getClass(), nodeName);
  }

  @Override
  public void save(@Nonnull Stage stage) {
    if (!preferences().getBoolean(FULL_SCREEN, false) &&
        !preferences().getBoolean(MAXIMIZED, false)) {
      boundsStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
    }
  }

  @Override
  public void update(@Nonnull Stage stage) {
    stage.maximizedProperty().addListener((observable, oldValue, newValue) -> preferences().putBoolean(MAXIMIZED, newValue));
    Optional.ofNullable(boundsStorage.get()).ifPresentOrElse(
        r -> {
          if (Screen.getPrimary().getVisualBounds().contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())) {
            stage.setX(r.getX());
            stage.setY(r.getY());
            stage.setWidth(r.getWidth());
            stage.setHeight(r.getHeight());
          }
          else {
            centerOnScreen(stage);
          }
        },
        () -> {
        }
    );
    stage.setMaximized(preferences().getBoolean(MAXIMIZED, false));
    stage.setFullScreen(preferences().getBoolean(FULL_SCREEN, false));
  }

  @Override
  public final Stage get() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final void delete() throws BackingStoreException {
    super.delete();
    boundsStorage.delete();
  }

  final void saveFullScreenState(boolean state) {
    preferences().putBoolean(FULL_SCREEN, state);
  }

  private static void centerOnScreen(@Nonnull Stage stage) {
    stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    stage.centerOnScreen();
  }
}
