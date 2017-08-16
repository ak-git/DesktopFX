package com.ak.fx.scene;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

final class LineDiagram extends AbstractRegion {
  private static final double LABEL_HEIGHT = GridCell.SMALL.getStep() / 3;
  private final Rectangle rectangle = new Rectangle();
  private final Text label = new Text("aVL");

  LineDiagram() {
    rectangle.setStroke(Color.BLACK);
    rectangle.setFill(null);
    rectangle.setStrokeWidth(3.0);
    label.setFont(new Font(Font.getDefault().getName(), LABEL_HEIGHT));
    getChildren().add(rectangle);
    getChildren().add(label);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    rectangle.setX(x);
    rectangle.setY(y);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
    label.relocate(x + GridCell.SMALL.getStep() + GridCell.POINTS.getStep(),
        y + height / 4 - LABEL_HEIGHT);
  }
}
