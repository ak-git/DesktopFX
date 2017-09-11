package com.ak.fx.scene;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static com.ak.fx.scene.Constants.LABEL_HEIGHT;
import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

final class LineDiagram extends AbstractRegion {
  private final Rectangle bounds = new Rectangle();
  private final Text title = new Text();
  private final Text centerValue = new Text("0");
  private final Polyline polyline = new Polyline();
  private double xStep = 1.0;
  private int nowIndex;

  LineDiagram(@Nonnull String name) {
    bounds.setVisible(false);
    bounds.setStroke(Color.BLACK);
    bounds.setFill(null);
    bounds.setStrokeWidth(2);

    title.setFont(Constants.FONT);
    title.setText(name);
    centerValue.setFont(title.getFont());

    polyline.setStroke(Color.BLACK);
    polyline.translateXProperty().setValue(SMALL.getStep());
    polyline.translateYProperty().bind(Bindings.divide(heightProperty(), 2));

    getChildren().add(bounds);
    getChildren().add(title);
    getChildren().add(centerValue);
    getChildren().add(polyline);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    bounds.setX(x);
    bounds.setY(y);
    bounds.setWidth(width);
    bounds.setHeight(height);
    title.relocate(x + POINTS.getStep() / 2, y + height / 4 - LABEL_HEIGHT - POINTS.getStep() / 4);
    centerValue.relocate(x + POINTS.getStep() / 4, y + height / 2 - LABEL_HEIGHT - POINTS.getStep() / 4);

    if (polyline.getPoints().size() / 2 > getMaxSamples()) {
      polyline.getPoints().remove(getMaxSamples() * 2, polyline.getPoints().size());
      nowIndex = 0;
    }
    polyline.setVisible(SMALL.maxWidth(width) > SMALL.getStep() * 2);
  }

  void setMean(@Nonnull String mean) {
    centerValue.setText(mean);
  }

  void setAll(@Nonnull double[] y) {
    polyline.getPoints().clear();
    for (int i = 0, n = Math.min(y.length, getMaxSamples()); i < n; i++) {
      polyline.getPoints().add(xStep * i);
      polyline.getPoints().add(-y[i]);
      nowIndex++;
    }

    if (y.length == 0) {
      nowIndex = 0;
    }
    else {
      nowIndex %= getMaxSamples();
    }
  }

  void add(double y) {
    if (polyline.getPoints().size() / 2 <= nowIndex) {
      polyline.getPoints().add(xStep * nowIndex);
      polyline.getPoints().add(y);
    }
    else {
      polyline.getPoints().set(nowIndex * 2 + 1, y);
    }

    nowIndex++;
    nowIndex %= getMaxSamples();
  }

  int getMaxSamples() {
    return Math.max(0, (int) Math.rint((SMALL.minCoordinate(getWidth()) + SMALL.maxWidth(getWidth()) -
        polyline.translateXProperty().get()) / xStep));
  }

  void setXStep(@Nonnegative double xStep) {
    this.xStep = xStep;
  }
}
