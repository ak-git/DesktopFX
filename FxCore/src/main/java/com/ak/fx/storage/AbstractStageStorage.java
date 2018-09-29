package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.ak.fx.util.FxUtils;
import com.ak.storage.AbstractStorage;
import com.ak.storage.LocalStorage;
import com.ak.storage.Storage;
import javafx.stage.Screen;
import javafx.stage.Stage;

abstract class AbstractStageStorage extends AbstractStorage<Stage> {
  private static final String FULL_SCREEN = "fullScreen";
  private static final String MAXIMIZED = "maximized";
  private static final String BOUNDS = "bounds";
  private final Storage<Boolean> fullScreenStorage;
  private final Storage<Boolean> maximizedStorage;
  private final Storage<Rectangle2D.Double> boundsStorage;

  AbstractStageStorage(@Nonnull String filePrefix) {
    super(filePrefix);
    fullScreenStorage = new LocalStorage<>(filePrefix, FULL_SCREEN, Boolean.class);
    maximizedStorage = new LocalStorage<>(filePrefix, MAXIMIZED, Boolean.class);
    boundsStorage = new LocalStorage<>(filePrefix, BOUNDS, Rectangle2D.Double.class);
  }

  @Override
  public void save(@Nonnull Stage stage) {
    if (!Optional.ofNullable(fullScreenStorage.get()).orElse(false) &&
        !Optional.ofNullable(maximizedStorage.get()).orElse(false)) {
      boundsStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
    }
  }

  @Override
  public void update(@Nonnull Stage stage) {
    stage.maximizedProperty().addListener((observable, oldValue, newValue) -> maximizedStorage.save(newValue));
    Optional.ofNullable(boundsStorage.get()).ifPresent(
        rectangle -> {
          javafx.geometry.Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
          if (visualBounds.contains(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight())) {
            stage.setX(rectangle.getX());
            stage.setY(rectangle.getY());
            stage.setWidth(rectangle.getWidth());
            stage.setHeight(rectangle.getHeight());
          }
          else {
            stage.setWidth(FxUtils.WIDTH_MIN);
            stage.setHeight(FxUtils.HEIGHT_MIN);
            stage.centerOnScreen();
          }
        }
    );
    Optional.ofNullable(maximizedStorage.get()).ifPresent(stage::setMaximized);
    Optional.ofNullable(fullScreenStorage.get()).ifPresent(stage::setFullScreen);
  }

  @Override
  public final Stage get() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final void delete() {
    Arrays.asList(fullScreenStorage, maximizedStorage, boundsStorage).forEach(Storage::delete);
  }

  final void saveFullScreenState(boolean state) {
    fullScreenStorage.save(state);
  }
}
