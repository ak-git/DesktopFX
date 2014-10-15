package com.ak.storage;

import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.Instant;

import javafx.stage.Stage;

public final class StageStorage extends AbstractStorage<Stage> {
  private static final String BOUNDS = "bounds";
  private static final String FULL_SCREEN = "fullScreen";
  private final Storage<Boolean> fullScreenStorage;
  private Instant fullScreenEventInstant = Instant.now();

  public StageStorage(String filePrefix) {
    super(filePrefix);
    fullScreenStorage = GenericStorage.newBooleanStorage(filePrefix, FULL_SCREEN);
  }

  @Override
  public void save(Stage stage) {
    if (!Duration.between(fullScreenEventInstant, Instant.now()).minusSeconds(3).isNegative()) {
      fullScreenStorage.save(stage.isFullScreen());
    }

    if (!fullScreenStorage.load(false)) {
      LocalStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()),
          fileName(BOUNDS));
    }
  }

  @Override
  public Stage load(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
      fullScreenStorage.save(oldValue);
      fullScreenEventInstant = Instant.now();
    });
    LocalStorage.load(fileName(BOUNDS), Rectangle2D.Double.class, rectangle -> {
      stage.setX(rectangle.getX());
      stage.setY(rectangle.getY());
      stage.setWidth(rectangle.getWidth());
      stage.setHeight(rectangle.getHeight());
    });
    stage.setFullScreen(fullScreenStorage.load(false));
    return stage;
  }
}
