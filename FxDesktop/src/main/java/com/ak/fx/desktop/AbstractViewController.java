package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.Filters;
import com.ak.fx.scene.AxisXController;
import com.ak.fx.scene.AxisYController;
import com.ak.fx.scene.Chart;
import io.reactivex.internal.util.EmptyComponent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class AbstractViewController<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>>
    implements Initializable, Subscriber<int[]> {
  @Nonnull
  private final GroupService<RESPONSE, REQUEST, EV> service;
  @Nonnull
  private Subscription subscription = EmptyComponent.INSTANCE;
  @Nullable
  @FXML
  private Chart chart;
  private final AxisXController axisXController = new AxisXController(this::changed);
  private final AxisYController<EV> axisYController = new AxisYController<>();

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
        axisXController.preventCenter(chart.widthProperty().doubleValue());
        event.consume();
      });
      chart.widthProperty().addListener((observable, oldValue, newValue) -> axisXController.preventCenter(newValue.doubleValue()));
      chart.heightProperty().addListener((observable, oldValue, newValue) -> changed());
      chart.diagramHeightProperty().addListener((observable, oldValue, newValue) -> axisYController.setLineDiagramHeight(newValue.doubleValue()));
      axisXController.setFrequency(service.getFrequency());
      axisXController.stepProperty().addListener((observable, oldValue, newValue) -> chart.setXStep(newValue.doubleValue()));
      axisYController.setVariables(service.getVariables());
    }
    service.subscribe(this);
  }

  @Override
  public final void onSubscribe(Subscription s) {
    changed();
    subscription.cancel();
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
    setAll(service.read(axisXController.getStart(), axisXController.getEnd()));
  }

  private void setAll(@Nonnull List<? extends int[]> chartData) {
    axisXController.checkLength(chartData.get(0).length);

    IntStream.range(0, chartData.size()).forEachOrdered(i -> {
      int[] values = Filters.filter(FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).build(), chartData.get(i));
      axisYController.scaleOrdered(values, scaleInfo ->
          Objects.requireNonNull(chart).setAll(i, IntStream.of(values).parallel().mapToDouble(scaleInfo).toArray(), scaleInfo)
      );
    });
  }
}
