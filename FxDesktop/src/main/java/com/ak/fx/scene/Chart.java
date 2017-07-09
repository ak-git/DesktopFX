package com.ak.fx.scene;

import javafx.scene.layout.Region;

public final class Chart extends Region {
  private final MilliGrid milliGrid = new MilliGrid();

  public Chart() {
    getChildren().addAll(milliGrid);
    // mark chartContent as unmanaged because any changes to its preferred size shouldn't cause a relayout
    milliGrid.setManaged(false);
  }

  @Override
  protected void layoutChildren() {
    double top = snappedTopInset();
    double left = snappedLeftInset();
    double bottom = snappedBottomInset();
    double right = snappedRightInset();
    double width = getWidth();
    double height = getHeight();
    milliGrid.resizeRelocate(left, top, width - left - right, height - top - bottom);
  }
}
