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
  @Nonnegative
  private double xStep = 1.0;
  @Nonnegative
  private int nowIndex;
  @Nonnegative
  private int maxSamples = 1;
  @Nonnull
  private DoubleFunction<String> positionToStringConverter = value -> Strings.EMPTY;

  LineDiagram(@Nonnull String name) {
    bounds.setVisible(false);
    bounds.setStroke(Color.BLACK);
    bounds.setFill(null);
    bounds.setStrokeWidth(2);

    visibleTextBounds.setVisible(false);

    title.setVisible(false);
    title.fontProperty().bind(Fonts.H1.fontProperty(this::getScene));
    title.setText(name);

    polyline.setStroke(Color.BLACK);
    polyline.translateYProperty().bind(Bindings.divide(heightProperty(), 2));
    polyline.setManaged(false);

    getChildren().addAll(bounds, visibleTextBounds, title, yLabels, polyline);
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
      var text = new Text();
      updateText(i, text);
      text.fontProperty().bind(Fonts.H2.fontProperty(this::getScene));
      text.relocate(POINTS.getStep() / 4, SMALL.getStep() * i - text.getFont().getSize() - POINTS.getStep() / 4);
      return text;
    }).collect(Collectors.toList()));

    for (var i = 0; i < yLabels.getChildren().size(); i++) {
      title.setVisible(false);
      if (yLabels.getChildren().get(i).isVisible()) {
        title.setVisible(true);
        title.relocate(x + SMALL.getStep() * 2.1, y + SMALL.getStep() * i - title.getFont().getSize() - POINTS.getStep() / 4);
        break;
      }
    }
  }

  void setAll(@Nonnull double[] y, @Nonnull DoubleFunction<String> positionToStringConverter) {
    this.positionToStringConverter = positionToStringConverter;
    for (var i = 0; i < yLabels.getChildren().size(); i++) {
      updateText(i, (Text) yLabels.getChildren().get(i));
    }

    if (polyline.getPoints().size() / 2 == y.length) {
      for (var i = 0; i < y.length; i++) {
        polyline.getPoints().set(i * 2 + 1, -y[i]);
      }
    }
    else {
      polyline.getPoints().clear();
      for (var i = 0; i < y.length; i++) {
        polyline.getPoints().add(xStep * i);
        polyline.getPoints().add(-y[i]);
      }
    }
    nowIndex = Math.min(y.length, maxSamples) % maxSamples;
  }

  void add(double y) {
    if (polyline.getPoints().size() / 2 <= nowIndex) {
      polyline.getPoints().add(xStep * nowIndex);
      polyline.getPoints().add(-y);
    }
    else {
      polyline.getPoints().set(nowIndex * 2 + 1, -y);
    }

    nowIndex++;
    nowIndex %= maxSamples;
  }

  void shiftRight(@Nonnull double[] y) {
    for (int i = y.length * 2 + 1; i < polyline.getPoints().size(); i += 2) {
      polyline.getPoints().set(i - y.length * 2, polyline.getPoints().get(i));
    }
    for (int i = polyline.getPoints().size() - (y.length * 2 - 1), n = 0; i < polyline.getPoints().size(); i += 2, n++) {
      polyline.getPoints().set(i, -y[n]);
    }
  }

  void shiftLeft(@Nonnull double[] y) {
    for (int i = polyline.getPoints().size() - (y.length * 2 + 1); i > 0; i -= 2) {
      polyline.getPoints().set(i + y.length * 2, polyline.getPoints().get(i));
    }
    for (int i = 1, n = 0; i < y.length * 2; i += 2, n++) {
      polyline.getPoints().set(i, -y[n]);
    }
  }

  void setXStep(@Nonnegative double xStep) {
    this.xStep = xStep;
  }

  void setMaxSamples(@Nonnegative int maxSamples) {
    this.maxSamples = Math.max(maxSamples, 1);
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
    var text = positionToStringConverter.apply(getHeight() / 2 - i * SMALL.getStep());
    label.setText(label.isVisible() ? text : Strings.EMPTY);
  }
}
