package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import javax.annotation.Nonnull;

import com.ak.fx.util.FxUtils;
import javafx.stage.Screen;
import javafx.stage.Stage;

abstract class AbstractStageStorage extends AbstractStorage<Stage> {
  private static final String FULL_SCREEN = "fullScreen";
  private static final String MAXIMIZED = "maximized";
  private final Storage<Rectangle2D.Double> boundsStorage = new BoundsStorage(getClass());

  AbstractStageStorage(@Nonnull Class<?> c) {
    super(c);
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
    Optional.ofNullable(boundsStorage.get()).ifPresent(
        r -> {
          if (Screen.getPrimary().getVisualBounds().contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())) {
            stage.setX(r.getX());
            stage.setY(r.getY());
            stage.setWidth(r.getWidth());
            stage.setHeight(r.getHeight());
          }
          else {
            stage.setWidth(FxUtils.WIDTH_MIN);
            stage.setHeight(FxUtils.HEIGHT_MIN);
            stage.centerOnScreen();
          }
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
}
