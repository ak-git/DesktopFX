package com.ak.fx.scene;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public final class Chart extends Region {
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<Rectangle> rectangles = Stream.generate(() -> {
    Rectangle rectangle = new Rectangle();
    rectangle.setStroke(Color.BLACK);
    rectangle.setFill(null);
    rectangle.setStrokeWidth(3.0);
    return rectangle;
  }).limit(3).collect(Collectors.toList());

  public Chart() {
    getChildren().addAll(milliGrid);
    getChildren().addAll(rectangles);
    // mark chartContent as unmanaged because any changes to its preferred size shouldn't cause a relayout
    milliGrid.setManaged(false);
    rectangles.get(0).setStroke(Color.RED);
    rectangles.get(1).setStroke(Color.YELLOW);
    rectangles.get(2).setStroke(Color.GREEN);
  }

  @Override
  protected void layoutChildren() {
    double top = snappedTopInset();
    double left = snappedLeftInset();
    double contentWidth = snapSize(getWidth() - (left + snappedRightInset()));
    double contentHeight = snapSize(getHeight() - (top + snappedBottomInset()));
    layoutChartChildren(snapPosition(left), snapPosition(top), contentWidth, contentHeight);
  }

  private void layoutChartChildren(double xInset, double yInset, double width, double height) {
    milliGrid.resizeRelocate(xInset, yInset, width, height);

    double chartHeight = GridCell.SMALL.maxCoordinate(height * 2 / (1 + rectangles.size()));
    for (Rectangle rectangle : rectangles) {
      rectangle.setX(xInset + GridCell.SMALL.minCoordinate(width));
      rectangle.setWidth(GridCell.SMALL.maxCoordinate(width));
      rectangle.setY(yInset + GridCell.SMALL.minCoordinate(height));
      rectangle.setHeight(chartHeight);
    }

    if (chartHeight >= GridCell.SMALL.getStep() * 2) {
      for (int i = 0; i < rectangles.size() / 2; i++) {
        rectangles.get(i).setY(yInset + GridCell.SMALL.minCoordinate(height) +
            GridCell.SMALL.roundCoordinate(height / (rectangles.size() + 1)) * i);
      }
      if ((rectangles.size() & 1) != 0) {
        rectangles.get(rectangles.size() / 2).setY(yInset + GridCell.SMALL.minCoordinate(height) +
            GridCell.SMALL.maxCoordinate(height) / 2 - chartHeight / 2
        );
      }
      for (int i = 0; i < rectangles.size() / 2; i++) {
        rectangles.get(rectangles.size() - 1 - i).setY(
            yInset + GridCell.SMALL.minCoordinate(height) + GridCell.SMALL.maxCoordinate(height) -
                chartHeight - GridCell.SMALL.roundCoordinate(height / (rectangles.size() + 1)) * i);
      }
    }
    else {
      rectangles.forEach(rectangle -> rectangle.setHeight(GridCell.SMALL.maxCoordinate(height)));
    }
  }
}
