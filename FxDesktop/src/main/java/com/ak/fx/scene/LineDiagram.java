package com.ak.fx.scene;

import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Strings;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

final class LineDiagram extends AbstractRegion {
  private final Rectangle bounds = new Rectangle();
  private final Rectangle visibleTextBounds = new Rectangle();
  private final Text title = new Text();
  private final Group yLabels = new Group();
  private final Polyline polyline = new Polyline();
  private double xStep = 1.0;
  @Nonnull
  private DoubleFunction<String> positionToStringConverter = value -> Strings.EMPTY;

  LineDiagram(@Nonnull String name) {
    bounds.setVisible(false);
    bounds.setStroke(Color.BLACK);
    bounds.setFill(null);
    bounds.setStrokeWidth(2);

    visibleTextBounds.setVisible(false);

    title.setVisible(false);
    title.fontProperty().bind(Fonts.H1.fontProperty());
    title.setText(name);

    polyline.setStroke(Color.BLACK);
    polyline.translateYProperty().bind(Bindings.divide(heightProperty(), 2));
    polyline.setManaged(false);

    getChildren().add(bounds);
    getChildren().add(visibleTextBounds);
    getChildren().add(title);
    getChildren().add(yLabels);
    getChildren().add(polyline);
  }

  @Override
  void layoutAll(double x, double y, double width, double height) {
    bounds.setX(x + getLayoutY());
    bounds.setY(y);
    bounds.setWidth(width - getLayoutY() * 2);
    bounds.setHeight(height);
    polyline.setVisible(SMALL.maxValue(width) > SMALL.getStep() * 2);

    yLabels.getChildren().clear();
    yLabels.getChildren().addAll(IntStream.range(0, (int) (Math.rint((height / 2) / SMALL.getStep()) * 2) + 1).mapToObj(i -> {
      Text text = new Text();
      updateText(i, text);
      text.fontProperty().bind(Fonts.H2.fontProperty());
      text.relocate(POINTS.getStep() / 4, SMALL.getStep() * i - text.getFont().getSize() - POINTS.getStep() / 4);
      return text;
    }).collect(Collectors.toList()));

    for (int i = 0; i < yLabels.getChildren().size(); i++) {
      title.setVisible(false);
      if (yLabels.getChildren().get(i).isVisible()) {
        title.setVisible(true);
        title.relocate(x + SMALL.getStep() * 1.5, y + SMALL.getStep() * i - title.getFont().getSize() - POINTS.getStep() / 4);
        break;
      }
    }
  }

  void setAll(@Nonnull double[] y, @Nonnull DoubleFunction<String> positionToStringConverter) {
    this.positionToStringConverter = positionToStringConverter;
    for (int i = 0; i < yLabels.getChildren().size(); i++) {
      updateText(i, (Text) yLabels.getChildren().get(i));
    }

    polyline.getPoints().clear();
    for (int i = 0; i < y.length; i++) {
      polyline.getPoints().add(xStep * i);
      polyline.getPoints().add(-y[i]);
    }
  }

  void setXStep(@Nonnegative double xStep) {
    this.xStep = xStep;
  }

  void setVisibleTextBounds(double y, double height) {
    visibleTextBounds.setX(SMALL.getStep() * 2 + getLayoutY());
    visibleTextBounds.setY(y);
    visibleTextBounds.setWidth(SMALL.getStep());
    visibleTextBounds.setHeight(height);
  }

  private void updateText(int i, Text label) {
    double posY = i * SMALL.getStep();
    label.setVisible(posY > visibleTextBounds.getY() && posY < visibleTextBounds.getY() + visibleTextBounds.getHeight());
    String text = positionToStringConverter.apply(getHeight() / 2 - i * SMALL.getStep());
    label.setText(label.isVisible() ? text : Strings.EMPTY);
  }
}
