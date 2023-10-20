package com.ak.fx.scene;

import jakarta.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleFunction;

import static com.ak.fx.scene.GridCell.*;

public final class Chart extends AbstractRegion {
  private final MilliGrid milliGrid = new MilliGrid();
  private final List<LineDiagram> lineDiagrams = new ArrayList<>();
  private final StackPane xAxisUnitGroup = new StackPane();
  private final Text xAxisUnit = new Text();
  private final Text banner = new Text();
  private final DoubleProperty diagramWidth = new SimpleDoubleProperty();
  private final DoubleProperty diagramHeight = new SimpleDoubleProperty();

  @Inject
  public Chart() {
    milliGrid.setManaged(false);
    getChildren().add(milliGrid);
    Rectangle rectangle = new Rectangle();
    rectangle.setFill(Fonts.WHITE_80);
    xAxisUnitGroup.getChildren().add(rectangle);
    xAxisUnitGroup.getChildren().add(xAxisUnit);
    xAxisUnit.textProperty().addListener((observable, oldValue, newValue) -> {
      double w = xAxisUnit.getBoundsInParent().getWidth();
      double h = xAxisUnit.getBoundsInParent().getHeight();
      double addSize = Math.min(w, h) * 0.5;
      rectangle.setWidth(w + addSize);
      rectangle.setHeight(h + addSize);
      rectangle.setArcWidth(addSize * 2.0);
      rectangle.setArcHeight(addSize * 2.0);
    });
    xAxisUnit.fontProperty().bind(Fonts.H2.fontProperty(this::getScene));
    banner.fontProperty().bind(Fonts.H1.fontProperty(this::getScene));
    banner.setTextAlignment(TextAlignment.RIGHT);
  }

  @Override
  void layoutAll(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    xAxisUnitGroup.relocate(x + BIG.minCoordinate(width) + BIG.maxValue(width) / 2,
        y + SMALL.minCoordinate(height) + SMALL.getStep() / 2 - xAxisUnit.getFont().getSize() / 2);

    banner.relocate(x + SMALL.minCoordinate(width) + SMALL.maxValue(width) - SMALL.getStep() - banner.getBoundsInParent().getWidth(),
        y + SMALL.minCoordinate(height) + SMALL.getStep() / 2 - xAxisUnit.getFont().getSize());

    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxValue(width), SMALL.maxValue(height));
    diagramWidth.set(SMALL.maxValue(width));
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    double n = lineDiagrams.size() == 1 ? 2 : lineDiagrams.size() + 2;
    double dHeight = SMALL.maxValue((height + POINTS.getStep()) * 2 / n);
    if (dHeight >= SMALL.getStep() * 2) {
      diagramHeight.setValue(dHeight);
      lineDiagrams.forEach(lineDiagram -> lineDiagram.resizeRelocate(x, y, width, dHeight));

      for (var i = 0; i < lineDiagrams.size() / 2; i++) {
        double v = SMALL.roundCoordinate(height / (lineDiagrams.size() + 1)) * i;
        lineDiagrams.get(i).relocate(x, y + v);
        lineDiagrams.get(lineDiagrams.size() - 1 - i).relocate(x, y + height - dHeight - v);
      }
      if ((lineDiagrams.size() & 1) != 0) {
        lineDiagrams.get(lineDiagrams.size() / 2).relocate(x, y + height / 2 - dHeight / 2);
      }

      for (var i = 0; i < lineDiagrams.size(); i++) {
        double visibleY = 0;
        if (i > 0) {
          double approveLineG = (lineDiagrams.get(i).getLayoutY() + lineDiagrams.get(i - 1).getLayoutY() + dHeight) / 2;
          visibleY = approveLineG - lineDiagrams.get(i).getLayoutY() - POINTS.getStep();
        }

        double visibleH = dHeight - visibleY + POINTS.getStep();
        if (i < lineDiagrams.size() - 1) {
          double approveLineG = (lineDiagrams.get(i).getLayoutY() + lineDiagrams.get(i + 1).getLayoutY() + dHeight) / 2;
          visibleH = approveLineG - lineDiagrams.get(i).getLayoutY() - POINTS.getStep() - visibleY;
        }
        lineDiagrams.get(i).setVisibleTextBounds(visibleY, visibleH);
      }
    }
    else {
      diagramHeight.setValue(height);
      lineDiagrams.forEach(lineDiagram -> {
        lineDiagram.resizeRelocate(x, y, width, height);
        lineDiagram.setVisibleTextBounds(height / 2, 0);
      });
    }
  }

  public void setVariables(@Nonnull Collection<String> variables) {
    lineDiagrams.addAll(variables.stream().map(LineDiagram::new).toList());
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setManaged(false));
    getChildren().addAll(lineDiagrams);
    getChildren().add(xAxisUnitGroup);
    getChildren().add(banner);
  }

  public void setXStep(@Nonnegative double xStep) {
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setXStep(xStep));
  }

  public void setMaxSamples(@Nonnegative int maxSamples) {
    lineDiagrams.forEach(lineDiagram -> lineDiagram.setMaxSamples(maxSamples));
  }

  public StringProperty titleProperty() {
    return xAxisUnit.textProperty();
  }

  public void setBannerText(@Nonnull String text) {
    banner.setText(text);
  }

  public ReadOnlyDoubleProperty diagramWidthProperty() {
    return diagramWidth;
  }

  public ReadOnlyDoubleProperty diagramHeightProperty() {
    return diagramHeight;
  }

  public void setAll(@Nonnegative int chartIndex, @Nonnull double[] values, @Nonnull DoubleFunction<String> positionToStringConverter) {
    lineDiagrams.get(chartIndex).setAll(values, positionToStringConverter);
  }
}
