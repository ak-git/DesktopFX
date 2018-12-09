package com.ak.fx.scene;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static com.ak.fx.scene.GridCell.BIG;
import static com.ak.fx.scene.GridCell.POINTS;
import static com.ak.fx.scene.GridCell.SMALL;

public final class Chart<EV extends Enum<EV> & Variable<EV>> extends AbstractRegion {
  private final AxisXController axisXController = new AxisXController(this::changed);
  private final AxisYController<EV> axisYController = new AxisYController<>();

  private final MilliGrid milliGrid = new MilliGrid();
  private final Map<EV, LineDiagram> lineDiagrams = new LinkedHashMap<>();
  private final Text xAxisUnit = new Text();
  private final Text banner = new Text();
  private final DoubleProperty diagramWidth = new SimpleDoubleProperty();
  private final DoubleProperty diagramHeight = new SimpleDoubleProperty();

  @Nonnull
  private Runnable changedCallback = () -> {
  };

  @Inject
  public Chart() {
    milliGrid.setManaged(false);
    getChildren().add(milliGrid);
    xAxisUnit.fontProperty().bind(Fonts.H2.fontProperty());
    banner.fontProperty().bind(Fonts.H1.fontProperty());
    banner.setTextAlignment(TextAlignment.RIGHT);
  }

  public AxisXController getAxisXController() {
    return axisXController;
  }

  public void init(@Nonnull Iterable<EV> variables, @Nonnegative double frequency, @Nonnull Runnable changedCallback) {
    for (EV variable : variables) {
      lineDiagrams.put(variable, new LineDiagram(Variables.toString(variable)));
    }
    lineDiagrams.values().forEach(lineDiagram -> lineDiagram.setManaged(false));
    getChildren().addAll(lineDiagrams.values());
    getChildren().add(xAxisUnit);
    getChildren().add(banner);

    xAxisUnit.textProperty().bind(axisXController.zoomProperty().asString());
    setOnScroll(event -> {
      axisXController.scroll(event.getDeltaX());
      event.consume();
    });
    setOnZoomStarted(event -> {
      axisXController.zoom(event.getZoomFactor());
      axisXController.preventEnd(diagramWidth.doubleValue());
      changed();
      event.consume();
    });
    diagramHeight.addListener((observable, oldValue, newValue) -> {
      axisYController.setLineDiagramHeight(newValue.doubleValue());
      changed();
    });
    diagramWidth.addListener((observable, oldValue, newValue) -> axisXController.preventEnd(newValue.doubleValue()));
    axisXController.stepProperty().addListener((observable, oldValue, newValue) ->
        lineDiagrams.values().forEach(lineDiagram -> lineDiagram.setXStep(newValue.doubleValue())));
    axisXController.setFrequency(frequency);
    this.changedCallback = changedCallback;
  }

  public void addAll(@Nonnull EV variable, @Nonnull int[] values) {
    ScaleYInfo<EV> scaleInfo = axisYController.scale(variable, values);
    lineDiagrams.get(variable).setAll(IntStream.of(values).unordered().parallel().mapToDouble(scaleInfo).toArray(), scaleInfo);
  }

  public void add(@Nonnull int[] visibleValues) {
  }

  @Override
  void layoutAll(double x, double y, double width, double height) {
    milliGrid.resizeRelocate(x, y, width, height);
    xAxisUnit.relocate(x + BIG.minCoordinate(width) + BIG.maxValue(width) / 2 + POINTS.getStep(),
        y + SMALL.minCoordinate(height) + SMALL.getStep() / 2 - xAxisUnit.getFont().getSize());

    banner.relocate(x + SMALL.minCoordinate(width) + SMALL.maxValue(width) - SMALL.getStep() - banner.getBoundsInParent().getWidth(),
        y + SMALL.minCoordinate(height) + SMALL.getStep() / 2 - xAxisUnit.getFont().getSize());

    layoutLineDiagrams(x + SMALL.minCoordinate(width), y + SMALL.minCoordinate(height), SMALL.maxValue(width), SMALL.maxValue(height));
    diagramWidth.set(SMALL.maxValue(width));
  }

  private void layoutLineDiagrams(double x, double y, double width, double height) {
    List<LineDiagram> diagrams = new ArrayList<>(lineDiagrams.values());
    double n = diagrams.size() == 1 ? 2 : diagrams.size() + 2;
    double dHeight = SMALL.maxValue((height + POINTS.getStep()) * 2 / n);
    if (dHeight >= SMALL.getStep() * 2) {
      diagramHeight.setValue(dHeight);
      diagrams.forEach(lineDiagram -> lineDiagram.resizeRelocate(x, y, width, dHeight));
      for (int i = 0; i < diagrams.size() / 2; i++) {
        diagrams.get(i).relocate(x, y + SMALL.roundCoordinate(height / (diagrams.size() + 1)) * i);
      }
      if ((diagrams.size() & 1) != 0) {
        diagrams.get(diagrams.size() / 2).relocate(x, y + height / 2 - dHeight / 2);
      }
      for (int i = 0; i < diagrams.size() / 2; i++) {
        diagrams.get(diagrams.size() - 1 - i).
            relocate(x, y + height - dHeight - SMALL.roundCoordinate(height / (diagrams.size() + 1)) * i);
      }

      for (int i = 0; i < diagrams.size(); i++) {
        double visibleY = 0;
        if (i > 0) {
          double approveLineG = (diagrams.get(i).getLayoutY() + diagrams.get(i - 1).getLayoutY() + dHeight) / 2;
          visibleY = approveLineG - diagrams.get(i).getLayoutY() - POINTS.getStep();
        }

        double visibleH = dHeight - visibleY + POINTS.getStep();
        if (i < diagrams.size() - 1) {
          double approveLineG = (diagrams.get(i).getLayoutY() + diagrams.get(i + 1).getLayoutY() + dHeight) / 2;
          visibleH = approveLineG - diagrams.get(i).getLayoutY() - POINTS.getStep() - visibleY;
        }
        diagrams.get(i).setVisibleTextBounds(visibleY, visibleH);
      }
    }
    else {
      diagramHeight.setValue(height);
      diagrams.forEach(lineDiagram -> {
        lineDiagram.resizeRelocate(x, y, width, height);
        lineDiagram.setVisibleTextBounds(height / 2, 0);
      });
    }
  }

  private void changed() {
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController.toString());
    changedCallback.run();
  }
}
