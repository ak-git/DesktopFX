package com.ak.fx.scene;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Chart extends AbstractRegion {
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> rectangles = Stream.generate(LineDiagram::new).limit(3).collect(Collectors.toList());

  public Chart() {
    getChildren().addAll(milliGrid);
    getChildren().addAll(rectangles);
  }

  @Override
  void layoutChartChildren(double xInset, double yInset, double width, double height) {
    milliGrid.resizeRelocate(xInset, yInset, width, height);
    layoutLineDiagrams(
        xInset + GridCell.SMALL.minCoordinate(width),
        yInset + GridCell.SMALL.minCoordinate(height),
        GridCell.SMALL.maxCoordinate(width), height);
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    double dHeight = GridCell.SMALL.maxCoordinate(height * 2 / (1 + rectangles.size()));

    for (LineDiagram rectangle : rectangles) {
      rectangle.resizeRelocate(x, y, width, dHeight);
    }

    if (dHeight >= GridCell.SMALL.getStep() * 2) {
      for (int i = 0; i < rectangles.size() / 2; i++) {
        rectangles.get(i).relocate(x, y + GridCell.SMALL.roundCoordinate(height / (rectangles.size() + 1)) * i);
      }
      if ((rectangles.size() & 1) != 0) {
        rectangles.get(rectangles.size() / 2).relocate(x, y + GridCell.SMALL.maxCoordinate(height) / 2 - dHeight / 2
        );
      }
      for (int i = 0; i < rectangles.size() / 2; i++) {
        rectangles.get(rectangles.size() - 1 - i).relocate(x,
            y + GridCell.SMALL.maxCoordinate(height) -
                dHeight - GridCell.SMALL.roundCoordinate(height / (rectangles.size() + 1)) * i);
      }
    }
    else {
      rectangles.forEach(rectangle -> rectangle.resize(width, GridCell.SMALL.maxCoordinate(height)));
    }
  }
}
