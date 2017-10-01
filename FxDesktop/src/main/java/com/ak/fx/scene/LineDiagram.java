package com.ak.fx.scene;

import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Strings;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

final class LineDiagram extends AbstractRegion {
  private final Rectangle bounds = new Rectangle();
  private final Text title = new Text();
  private final ObservableList<Text> yLabels = FXCollections.observableArrayList();
  private final Polyline polyline = new Polyline();
  private double xStep = 1.0;
  private int nowIndex;
  @Nonnull
  private DoubleFunction<String> positionToStringConverter = value -> Strings.EMPTY;

  LineDiagram(@Nonnull String name) {
    bounds.setVisible(false);
    bounds.setStroke(Color.BLACK);
    bounds.setFill(null);
    bounds.setStrokeWidth(2);

    title.setVisible(false);
    title.fontProperty().bind(Fonts.H1.fontProperty());
    title.setText(name);

    polyline.setStroke(Color.BLACK);
    polyline.translateYProperty().bind(Bindings.divide(heightProperty(), 2));
    polyline.setManaged(false);

    getChildren().add(bounds);
    getChildren().add(title);
    getChildren().add(polyline);

    heightProperty().addListener((observable, oldValue, newValue) -> {
      int count = (int) (Math.rint((newValue.doubleValue() / 2) / SMALL.getStep()) * 2) + 1;
      if (count != yLabels.size()) {
        getChildren().removeAll(yLabels);
        yLabels.clear();
        yLabels.addAll(IntStream.range(0, count).mapToObj(i -> {
          Text text = new Text(getText(i));
          text.fontProperty().bind(Fonts.H2.fontProperty());
          return text;
        }).collect(Collectors.toList()));
        getChildren().addAll(yLabels);
      }
    });
    yLabels.addListener((ListChangeListener<Text>) c -> {
      for (int i = 0; i < yLabels.size(); i++) {
        yLabels.get(i).relocate(POINTS.getStep() / 4, SMALL.getStep() * i -
            yLabels.get(i).getFont().getSize() - POINTS.getStep() / 4);
      }
    });
  }

  @Override
  void layoutAll(double x, double y, double width, double height) {
    bounds.setX(x);
    bounds.setY(y);
    bounds.setWidth(width);
    bounds.setHeight(height);

    if (polyline.getPoints().size() / 2 > getMaxSamples()) {
      polyline.getPoints().remove(getMaxSamples() * 2, polyline.getPoints().size());
      nowIndex = 0;
    }
    polyline.setVisible(SMALL.maxValue(width) > SMALL.getStep() * 2);
  }

  void setAll(@Nonnull double[] y, @Nonnull DoubleFunction<String> positionToStringConverter) {
    this.positionToStringConverter = positionToStringConverter;
    for (int i = 0; i < yLabels.size(); i++) {
      yLabels.get(i).setText(getText(i));
    }
    
    polyline.getPoints().clear();
    nowIndex = Math.min(y.length, getMaxSamples());
    for (int i = 0; i < nowIndex; i++) {
      polyline.getPoints().add(xStep * i);
      polyline.getPoints().add(-y[i]);
    }
    nowIndex %= getMaxSamples();
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

  void setXStep(@Nonnegative double xStep) {
    this.xStep = xStep;
  }

  private int getMaxSamples() {
    return Math.max(1, (int) Math.rint(getWidth() / xStep));
  }

  private String getText(int i) {
    return positionToStringConverter.apply(getHeight() / 2 - i * SMALL.getStep());
  }
}
