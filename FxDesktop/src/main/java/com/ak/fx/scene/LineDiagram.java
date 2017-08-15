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
  void layoutChartChildren(double xInset, double yInset, double width, double height) {
    rectangle.setX(xInset);
    rectangle.setY(yInset);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
  }
}
