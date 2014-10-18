package com.ak.fx.storage;

import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.ak.storage.AbstractStorage;
import com.ak.storage.LocalStorage;
import com.ak.storage.Storage;
import javafx.stage.Stage;

public final class StageStorage extends AbstractStorage<Stage> {
  private static final String BOUNDS = "bounds";
  private static final String FULL_SCREEN = "fullScreen";
  private final Storage<Boolean> fullScreenStorage;
  private final Storage<Rectangle2D.Double> boundsStorage;
  private Instant fullScreenEventInstant = Instant.now();

  public StageStorage(String filePrefix) {
    super(filePrefix);
    fullScreenStorage = new LocalStorage<>(filePrefix, FULL_SCREEN, Boolean.class);
    boundsStorage = new LocalStorage<>(filePrefix, BOUNDS, Rectangle2D.Double.class);
  }

  @Override
  public void save(Stage stage) {
    if (!Duration.between(fullScreenEventInstant, Instant.now()).minusSeconds(3).isNegative()) {
      fullScreenStorage.save(stage.isFullScreen());
    }
    Optional.ofNullable(fullScreenStorage.get()).ifPresent(fullScreen -> {
      if (!fullScreen) {
        boundsStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
      }
    });
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
      fullScreenStorage.save(oldValue);
      fullScreenEventInstant = Instant.now();
    });
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
  public Stage get() {
    throw new UnsupportedOperationException();
  }
}
