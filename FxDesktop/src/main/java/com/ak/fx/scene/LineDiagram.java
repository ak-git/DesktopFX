package com.ak.fx.scene;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Strings;
import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

final class LineDiagram extends AbstractRegion {
  private final Rectangle bounds = new Rectangle();
  private final Text title = new Text();
  private final Map<Integer, Text> yLabels = new HashMap<>();
  private final Polyline polyline = new Polyline();
  private double xStep = 1.0;
  private int nowIndex;
  @Nonnull
  private IntFunction<String> yLabelsGenerator = value -> Strings.EMPTY;

  LineDiagram(@Nonnull String name) {
    bounds.setVisible(false);
    bounds.setStroke(Color.BLACK);
    bounds.setFill(null);
    bounds.setStrokeWidth(2);

    title.setVisible(false);
    title.setFont(Constants.FONT_H1);
    title.setText(name);

    polyline.setStroke(Color.BLACK);
    polyline.translateXProperty().setValue(0);
    polyline.translateYProperty().bind(Bindings.divide(heightProperty(), 2));

    getChildren().add(bounds);
    getChildren().add(title);
    getChildren().add(polyline);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    bounds.setX(x);
    bounds.setY(y);
    bounds.setWidth(width);
    bounds.setHeight(height);

    getChildren().removeAll(yLabels.values());
    yLabels.clear();
    for (int i = 0; height / 2 - SMALL.getStep() * i > SMALL.minCoordinate(height); i++) {
      newYLabel(i, x, y + height / 2);
      if (yLabels.containsKey(i)) {
        title.setVisible(true);
        title.relocate(x + SMALL.getStep() * 1.5, y + height / 2 - SMALL.getStep() * i - title.getFont().getSize() - POINTS.getStep() / 4);
      }
    }
    for (int i = 1; height / 2 - SMALL.getStep() * i > SMALL.minCoordinate(height) - POINTS.getStep(); i++) {
      newYLabel(-i, x, y + height / 2);
    }
    getChildren().addAll(yLabels.values());

    if (polyline.getPoints().size() / 2 > getMaxSamples()) {
      polyline.getPoints().remove(getMaxSamples() * 2, polyline.getPoints().size());
      nowIndex = 0;
    }
    polyline.setVisible(SMALL.maxWidth(width) > SMALL.getStep() * 2);
  }

  void setYLabelsGenerator(@Nonnull IntFunction<String> yLabelsGenerator) {
    this.yLabelsGenerator = yLabelsGenerator;
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
    return Math.max(0, (int) Math.rint((getWidth() - polyline.translateXProperty().get()) / xStep));
  }

  double getCenter() {
    return getLayoutY() + getHeight() / 2.0;
  }

  void setXStep(@Nonnegative double xStep) {
    this.xStep = xStep;
  }

  private void newYLabel(int index, double x, double y) {
    String apply = yLabelsGenerator.apply(index * 10);
    if (!apply.isEmpty()) {
      Text label = new Text(apply);
      label.setFont(Constants.FONT_H2);
      label.relocate(x + POINTS.getStep() / 4, y - SMALL.getStep() * index - label.getFont().getSize() - POINTS.getStep() / 4);
      yLabels.put(index, label);
    }
  }
}
