package com.ak.fx.scene;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public final class Chart extends Region {
  private final MilliGrid milliGrid = new MilliGrid();
  private final Rectangle rectangle = new Rectangle();

  public Chart() {
    getChildren().addAll(milliGrid, rectangle);
    // mark chartContent as unmanaged because any changes to its preferred size shouldn't cause a relayout
    milliGrid.setManaged(false);

    rectangle.setStroke(Color.BLACK);
    rectangle.setFill(null);
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

    rectangle.setX(xInset + GridCell.BIG.minCoordinate(width));
    rectangle.setY(yInset + GridCell.BIG.minCoordinate(height));
    rectangle.setWidth(GridCell.BIG.maxCoordinate(width));
    rectangle.setHeight(GridCell.BIG.maxCoordinate(height));
  }
}
