package com.ak.fx.scene;

import javafx.scene.layout.Region;

abstract class AbstractRegion extends Region {
  @Override
  protected final void layoutChildren() {
    double top = snappedTopInset();
    double left = snappedLeftInset();
    double contentWidth = snapSize(getWidth() - (left + snappedRightInset()));
    double contentHeight = snapSize(getHeight() - (top + snappedBottomInset()));
    layoutAll(snapPosition(left), snapPosition(top), contentWidth, contentHeight);
  }

  abstract void layoutAll(double x, double y, double width, double height);
}
