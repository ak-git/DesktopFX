package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Flow;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Timer;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.Filters;
import com.ak.fx.scene.AxisXController;
import com.ak.fx.scene.AxisYController;
import com.ak.fx.scene.Chart;
import com.ak.fx.scene.ScaleYInfo;
import com.ak.fx.util.FxUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

public abstract class AbstractViewController<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    implements Initializable, Flow.Subscriber<int[]> {
  @Nonnull
  private final GroupService<RESPONSE, REQUEST, EV> service;
  private final AxisXController axisXController = new AxisXController(new IntConsumer() {
    private final Timer timer = new Timer(100, e -> changed());
    private boolean posDirection;

    @Override
    public void accept(int value) {
      if (posDirection == (value > 0)) {
        timer.stop();
        timer.setRepeats(false);
        timer.start();
        Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController.toString());
        if (value > 0) {
          List<? extends int[]> chartData = service.read(axisXController.getEnd() - value, axisXController.getEnd());
          for (int i = 0; i < chartData.size(); i++) {
            int[] values = Filters.filter(FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).build(), chartData.get(i));
            Objects.requireNonNull(chart).add(i, IntStream.of(values).parallel().mapToDouble(axisYController.getScale(service.getVariables().get(i))).toArray());
          }
          check(value, chartData.get(0).length);
        }
        else {
          value = Math.abs(value);
          List<? extends int[]> chartData = service.read(axisXController.getStart(), axisXController.getStart() + value);
          for (int i = 0; i < chartData.size(); i++) {
            int[] values = Filters.filter(FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).build(), chartData.get(i));
            Objects.requireNonNull(chart).prev(i, IntStream.of(values).parallel().mapToDouble(axisYController.getScale(service.getVariables().get(i))).toArray());
          }
          check(value, chartData.get(0).length);
        }
      }
      else {
        posDirection = !posDirection;
        changed();
      }
    }

    private void check(@Nonnegative int needSize, @Nonnegative int realSize) {
      if (realSize < needSize) {
        axisXController.checkLength(axisXController.getEnd() - axisXController.getStart() - (needSize - realSize));
      }
    }
  });
  private final AxisYController<EV> axisYController = new AxisYController<>();
  @Nullable
  private Flow.Subscription subscription;
  @Nullable
  @FXML
  private Chart chart;

  public AbstractViewController(@Nonnull GroupService<RESPONSE, REQUEST, EV> service) {
    this.service = service;
  }

  @Override
  public final void initialize(@Nullable URL location, @Nullable ResourceBundle resources) {
    if (chart != null) {
      chart.setOnDragOver(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
          event.acceptTransferModes(TransferMode.COPY);
        }
        else {
          event.consume();
        }
      });
      chart.setOnDragDropped(event -> {
        Dragboard db = event.getDragboard();
        boolean ok = false;
        if (db.hasFiles()) {
          for (File file : db.getFiles()) {
            if (service.accept(file)) {
              ok = true;
              break;
            }
          }
        }
        event.setDropCompleted(ok);
        event.consume();
      });
      chart.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        if (KeyCombination.keyCombination("Shortcut+N").match(event)) {
          service.refresh();
        }
      });
      chart.setVariables(service.getVariables().stream().map(Variables::toString).collect(Collectors.toList()));
      chart.titleProperty().bind(axisXController.zoomProperty().asString());
      chart.setOnScroll(event -> {
        axisXController.scroll(event.getDeltaX());
        event.consume();
      });
      chart.setOnZoomStarted(event -> {
        axisXController.zoom(event.getZoomFactor());
        axisXController.preventCenter(chart.diagramWidthProperty().doubleValue());
        event.consume();
      });
      chart.diagramHeightProperty().addListener((observable, oldValue, newValue) -> {
        axisYController.setLineDiagramHeight(newValue.doubleValue());
        changed();
      });
      chart.diagramWidthProperty().addListener((observable, oldValue, newValue) -> axisXController.preventCenter(newValue.doubleValue()));
      axisXController.stepProperty().addListener((observable, oldValue, newValue) -> chart.setXStep(newValue.doubleValue()));
      axisXController.setFrequency(service.getFrequency());
    }
    service.subscribe(this);
  }

  @Override
  public final void onSubscribe(Flow.Subscription s) {
    changed();
    if (subscription != null) {
      subscription.cancel();
    }
    subscription = s;
  }

  @Override
  public final void onNext(int[] ints) {
  }

  @Override
  public final void onError(Throwable t) {
    Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t);
  }

  @Override
  public final void onComplete() {
    changed();
  }

  private void changed() {
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController.toString());
    List<? extends int[]> chartData = service.read(axisXController.getStart(), axisXController.getEnd());
    FxUtils.invokeInFx(() -> {
      IntStream.range(0, chartData.size()).forEachOrdered(i -> {
        int[] values = Filters.filter(FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).build(), chartData.get(i));
        ScaleYInfo<EV> scaleInfo = axisYController.scale(service.getVariables().get(i), values);
        Objects.requireNonNull(chart).setAll(i, IntStream.of(values).parallel().mapToDouble(scaleInfo).toArray(), scaleInfo);
      });
      axisXController.checkLength(chartData.get(0).length);
    });
  }
}
