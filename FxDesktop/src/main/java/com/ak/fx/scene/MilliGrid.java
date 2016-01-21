package com.ak.fx.scene;

import java.awt.Toolkit;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;

public final class MilliGrid extends Pane {
  private enum GridCell {
    POINTS(1.0) {
      @Override
      double getStep() {
        return super.getStep() / 4.0;
      }

      @Override
      Path newPath() {
        Path path = super.newPath();
        path.setStrokeDashOffset(getStep());
        path.getStrokeDashArray().addAll(0.0, getStep() * 2, 0.0, getStep(), 0.0, getStep());
        return path;
      }
    },
    SMALL(1.0),
    BIG(3.0) {
      @Override
      double getStep() {
        return super.getStep() * 5.0;
      }
    };

    private static final double CENTIMETER = Toolkit.getDefaultToolkit().getScreenResolution() / 2.54;
    private static final Paint COLOR = new Color(225.0 / 255.0, 130.0 / 255.0, 110.0 / 255.0, 1.0);
    private final double strokeWidth;

    GridCell(double strokeWidth) {
      this.strokeWidth = strokeWidth;
    }

    final double getStrokeWidth() {
      return strokeWidth;
    }

    double getStep() {
      return CENTIMETER;
    }

    Path newPath() {
      Path p = new Path();
      p.setStroke(COLOR);
      p.setStrokeWidth(getStrokeWidth());
      return p;
    }

    static Map<GridCell, Path> newPaths() {
      Map<GridCell, Path> map = new EnumMap<>(GridCell.class);
      EnumSet.allOf(GridCell.class).forEach(gridCell -> map.put(gridCell, gridCell.newPath()));
      return Collections.unmodifiableMap(map);
    }
  }

  private final GridLine[] gridLines = {new HorizontalGridLine(), new VerticalGridLine()};
  private final Map<GridCell, Path> paths = GridCell.newPaths();

  public MilliGrid() {
    getChildren().addAll(paths.values());
  }

  @Override
  protected void layoutChildren() {
    paths.values().forEach(path -> path.getElements().clear());

    for (GridCell gridCell : paths.keySet()) {
      for (GridLine gridLine : gridLines) {
        gridLine.addToPath(paths.get(gridCell), gridCell);
        if (gridCell == GridCell.POINTS) {
          break;
        }
      }
    }
    super.layoutChildren();
  }

  private interface GridLine {
    void addToPath(Path path, GridCell gridCell);

    double contentSize();

    PathElement moveTo(double c, GridCell gridCell);

    PathElement lineTo(GridCell gridCell);
  }

  private abstract class AbstractGridLine implements GridLine {
    @Override
    public final void addToPath(Path path, GridCell gridCell) {
      double contentSize = contentSize();
      int factor = (int) Math.round(GridCell.SMALL.getStep() / gridCell.getStep());
      int i = 0;
      for (double c = minCoordinate(contentSize, gridCell); c < maxCoordinate(contentSize) + 1.0; c += gridCell.getStep()) {
        if (!(gridCell == GridCell.POINTS && i % factor == 0)) {
          path.getElements().addAll(moveTo(c, gridCell), lineTo(gridCell));
        }
        i++;
      }
    }

    final double contentWidth() {
      return snapSize(getWidth() - (snappedLeftInset() + snappedRightInset()));
    }

    final double contentHeight() {
      return snapSize(getHeight() - (snappedTopInset() + snappedBottomInset()));
    }

    final double minCoordinate(double size, GridCell gridCell) {
      double min = size / 2.0 - Math.floor(size / 2 / gridCell.getStep()) * gridCell.getStep();
      if (gridCell != GridCell.SMALL) {
        min = Math.max(min, minCoordinate(size, GridCell.SMALL));
      }
      return min;
    }

    final double maxCoordinate(double size) {
      return size - minCoordinate(size, GridCell.SMALL);
    }

    final double linePad(GridCell gridCell) {
      return (gridCell.getStrokeWidth() - 1.0) / 2.0;
    }
  }

  private final class VerticalGridLine extends AbstractGridLine {
    @Override
    public double contentSize() {
      return contentWidth();
    }

    @Override
    public PathElement moveTo(double x, GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + x, snappedTopInset() + minCoordinate(contentHeight(), GridCell.SMALL) + linePad(gridCell));
    }

    @Override
    public PathElement lineTo(GridCell gridCell) {
      return new VLineTo(snappedTopInset() + maxCoordinate(contentHeight()) - linePad(gridCell));
    }
  }

  private final class HorizontalGridLine extends AbstractGridLine {
    @Override
    public double contentSize() {
      return contentHeight();
    }

    @Override
    public PathElement moveTo(double y, GridCell gridCell) {
      return new MoveTo(snappedLeftInset() + minCoordinate(contentWidth(), GridCell.SMALL) + linePad(gridCell), snappedTopInset() + y);
    }

    @Override
    public PathElement lineTo(GridCell gridCell) {
      return new HLineTo(snappedLeftInset() + maxCoordinate(contentWidth()) - linePad(gridCell));
    }
  }
}
