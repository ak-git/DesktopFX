package com.ak.fx.scene;

import javafx.scene.layout.Region;

abstract class AbstractRegion extends Region {
  @Override
  protected final void layoutChildren() {
    double top = snappedTopInset();
    double left = snappedLeftInset();
    double contentWidth = snapSizeX(getWidth() - (left + snappedRightInset()));
    double contentHeight = snapSizeY(getHeight() - (top + snappedBottomInset()));
    layoutAll(snapPositionX(left), snapPositionY(top), contentWidth, contentHeight);
  }

  abstract void layoutAll(double x, double y, double width, double height);
}
