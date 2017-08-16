package com.ak.fx.scene;

import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

final class LineDiagram extends AbstractRegion {
  private static final double LABEL_HEIGHT = SMALL.getStep() / 3;
  private final Rectangle bounds = new Rectangle();
  private final Text title = new Text("aVL");
  private final Text centerValue = new Text("0");
  private final Path path = new Path();

  LineDiagram() {
    bounds.setVisible(false);
    bounds.setStroke(Color.BLACK);
    bounds.setFill(null);
    bounds.setStrokeWidth(3.0);

    title.setFont(Font.font(Font.getDefault().getName(), LABEL_HEIGHT));
    centerValue.setFont(title.getFont());

    path.setFill(Color.BLACK);
    getChildren().add(bounds);
    getChildren().add(title);
    getChildren().add(centerValue);
    getChildren().add(path);

    path.getElements().add(new MoveTo(0, 0));
    path.getElements().add(new LineTo(10, -200));
    path.translateXProperty().setValue(SMALL.getStep() * 2);
    path.translateYProperty().bind(Bindings.divide(heightProperty(), 2));
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    bounds.setX(x);
    bounds.setY(y);
    bounds.setWidth(width);
    bounds.setHeight(height);
    title.relocate(x + GridCell.SMALL.getStep() + POINTS.getStep(), y + height / 4 - LABEL_HEIGHT);
    centerValue.relocate(x + POINTS.getStep() / 4, y + height / 2 - LABEL_HEIGHT - POINTS.getStep() / 4);
  }
}
