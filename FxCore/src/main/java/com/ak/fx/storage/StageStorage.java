package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.util.Optional;

import com.ak.storage.AbstractStorage;
import com.ak.storage.LocalStorage;
import com.ak.storage.Storage;
import javafx.stage.Stage;

class StageStorage extends AbstractStorage<Stage> {
  private static final String FULL_SCREEN = "fullScreen";
  private static final String BOUNDS = "bounds";
  private final Storage<Boolean> fullScreenStorage;
  private final Storage<Rectangle2D.Double> boundsStorage;

  StageStorage(String filePrefix) {
    super(filePrefix);
    fullScreenStorage = new LocalStorage<>(filePrefix, FULL_SCREEN, Boolean.class);
    boundsStorage = new LocalStorage<>(filePrefix, BOUNDS, Rectangle2D.Double.class);
  }

  @Override
  public void save(Stage stage) {
    Optional.ofNullable(fullScreenStorage.get()).ifPresent(fullScreen -> {
      if (!fullScreen) {
        boundsStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
      }
    });
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> saveFullScreenState(oldValue));
    Optional.ofNullable(boundsStorage.get()).ifPresent(
        rectangle -> {
          stage.setX(rectangle.getX());
          stage.setY(rectangle.getY());
          stage.setWidth(rectangle.getWidth());
          stage.setHeight(rectangle.getHeight());
        }
    );
    Optional.ofNullable(fullScreenStorage.get()).ifPresent(stage::setFullScreen);
  }

  @Override
  public final Stage get() {
    throw new UnsupportedOperationException();
  }

  final void saveFullScreenState(boolean state) {
    fullScreenStorage.save(state);
  }
}
