package com.ak.fx.scene;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

final class LineDiagram extends AbstractRegion {
  private final Rectangle rectangle = new Rectangle();

  LineDiagram() {
    rectangle.setStroke(Color.BLACK);
    rectangle.setFill(null);
    rectangle.setStrokeWidth(3.0);
    getChildren().add(rectangle);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    rectangle.setX(x);
    rectangle.setY(y);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
  }
}
