package com.ak.fx.scene;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Chart extends AbstractRegion {
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = Stream.generate(LineDiagram::new).limit(3).collect(Collectors.toList());

  public Chart() {
    getChildren().addAll(milliGrid);
    getChildren().addAll(lineDiagrams);
  }

  @Override
  void layoutChartChildren(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    layoutLineDiagrams(x + GridCell.SMALL.minCoordinate(width), y + GridCell.SMALL.minCoordinate(height),
        GridCell.SMALL.maxCoordinate(width), height);
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    double dHeight = GridCell.SMALL.maxCoordinate(height * 2 / (1 + lineDiagrams.size()));

    for (LineDiagram rectangle : lineDiagrams) {
      rectangle.resizeRelocate(x, y, width, dHeight);
    }

    if (dHeight >= GridCell.SMALL.getStep() * 2) {
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(i).relocate(x, y + GridCell.SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
      if ((lineDiagrams.size() & 1) != 0) {
        lineDiagrams.get(lineDiagrams.size() / 2).relocate(x, y + GridCell.SMALL.maxCoordinate(height) / 2 - dHeight / 2
        );
      }
      for (int i = 0; i < lineDiagrams.size() / 2; i++) {
        lineDiagrams.get(lineDiagrams.size() - 1 - i).relocate(x,
            y + GridCell.SMALL.maxCoordinate(height) -
                dHeight - GridCell.SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i);
      }
    }
    else {
      lineDiagrams.forEach(rectangle -> rectangle.resize(width, GridCell.SMALL.maxCoordinate(height)));
    }
  }
}
