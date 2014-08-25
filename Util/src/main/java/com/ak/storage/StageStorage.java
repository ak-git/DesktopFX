package com.ak.storage;

import java.awt.geom.Rectangle2D;

import javafx.stage.Stage;

public final class StageStorage extends AbstractStorage<Stage> {
  private static final String BOUNDS_XML = "%s_bounds";

  public StageStorage(String fileName) {
    super(fileName);
  }

  @Override
  public void save(Stage stage) {
    LocalStorage.save(new Rectangle2D.Double(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()),
        String.format(BOUNDS_XML, fileName()));
  }

  @Override
  public Stage load(Stage stage) {
    LocalStorage.load(String.format(BOUNDS_XML, fileName()), Rectangle2D.Double.class, rectangle -> {
      stage.setX(rectangle.getX());
      stage.setY(rectangle.getY());
      stage.setWidth(rectangle.getWidth());
      stage.setHeight(rectangle.getHeight());
    });
    return stage;
  }
}
