package com.ak.fx.desktop;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.file.RecursiveWatcher;
import com.ak.fx.scene.AxisXController;
import com.ak.fx.scene.AxisYController;
import com.ak.fx.scene.Chart;
import com.ak.fx.scene.ScaleYInfo;
import com.ak.fx.util.FxUtils;
import com.ak.logging.OutputBuilders;
import com.ak.util.Extension;
import com.ak.util.Strings;
import jakarta.inject.Provider;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractViewController<T, R, V extends Enum<V> & Variable<V>>
    implements Initializable, Flow.Subscriber<int[]>, AutoCloseable, ViewController {
  private final GroupService<T, R, V> service;
  private final AxisXController axisXController = new AxisXController(this::changed);
  private final AxisYController<V> axisYController = new AxisYController<>();
  private Flow.@Nullable Subscription subscription;
  @FXML
  private @Nullable Chart chart;
  private long countSamples;
  private @Nullable SequentialTransition transition;
  private Closeable fileWatcher = () -> {
  };

  protected AbstractViewController(Provider<BytesInterceptor<T, R>> interceptorProvider,
                                   Provider<Converter<R, V>> converterProvider) {
    service = new GroupService<>(interceptorProvider::get, converterProvider::get);
    try {
      fileWatcher = new RecursiveWatcher(
          OutputBuilders.NONE.build(Strings.EMPTY).getPath(),
          path -> Converter.doConvert(interceptorProvider.get(), converterProvider.get(), path),
          Extension.BIN
      );
    }
    catch (IOException e) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public final void initialize(URL location, ResourceBundle resources) {
    if (chart != null) {
      chart.setOnDragDropped(event -> {
        event.setDropCompleted(event.getDragboard().getFiles().stream().anyMatch(service::accept));
        event.consume();
      });
      chart.setVariables(service.getVariables().stream()
          .filter(v -> v.options().contains(Variable.Option.VISIBLE))
          .map(Variables::toString)
          .toList()
      );
      chart.setBannerNames(service.getVariables().stream()
          .filter(v -> v.options().contains(Variable.Option.TEXT_VALUE_BANNER))
          .map(Variables::toString)
          .collect(Collectors.joining(Strings.NEW_LINE_2))
      );
      chart.setBannerUnits(service.getVariables().stream()
          .filter(v -> v.options().contains(Variable.Option.TEXT_VALUE_BANNER))
          .map(v -> Variables.fixUnit(v.getUnit()))
          .collect(Collectors.joining(Strings.NEW_LINE_2))
      );
      chart.titleProperty().bind(axisXController.zoomBinding());
      chart.diagramHeightProperty().addListener((_, _, newValue) -> {
        axisYController.setLineDiagramHeight(newValue.doubleValue());
        changed();
      });
      chart.diagramWidthProperty().addListener((_, _, newValue) -> axisXController.preventEnd(newValue.doubleValue()));

      axisXController.stepProperty().addListener((_, _, newValue) -> chart.setXStep(newValue.doubleValue()));
      axisXController.startProperty().addListener((_, _, _) -> changed());
      axisXController.lengthProperty().addListener((_, _, newValue) ->
          chart.setMaxSamples(newValue.intValue() / axisXController.getDecimateFactor())
      );
      axisXController.setFrequency(service.getFrequency());
    }

    var timeline = new Timeline(
        new KeyFrame(Duration.millis(50),
            (ActionEvent _) -> {
              int start = (int) (Math.max(0, countSamples - axisXController.getLength()));
              axisXController.setStart(start);
              if (start == 0) {
                changed();
              }
            })
    );
    timeline.setCycleCount(Animation.INDEFINITE);
    transition = new SequentialTransition(timeline);
    service.subscribe(this);
  }

  @Override
  public void onSubscribe(Flow.Subscription s) {
    if (subscription != null) {
      subscription.cancel();
    }
    subscription = Objects.requireNonNull(s);
    subscription.request(axisXController.getLength());
    countSamples = 0;
    Objects.requireNonNull(transition).play();
    changed();
    CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(this::changed);
  }

  @Override
  public void onNext(int[] ints) {
    countSamples++;
    displayBanner(ints);
  }

  @Override
  public final void onError(Throwable t) {
    Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t);
  }

  @Override
  public final void onComplete() {
    Objects.requireNonNull(transition).stop();
    changed();
  }

  @Override
  public void close() throws IOException {
    try {
      fileWatcher.close();
    }
    finally {
      service.close();
    }
  }

  @Override
  public void refresh(boolean force) {
    service.refresh(force);
    countSamples = 0;
    Objects.requireNonNull(transition).play();
    changed();
  }

  @Override
  public final void zoom(double zoomFactor) {
    if (chart != null) {
      axisXController.zoom(zoomFactor);
      axisXController.preventEnd(chart.diagramWidthProperty().doubleValue());
      changed();
    }
  }

  @Override
  public final void scroll(double deltaX) {
    axisXController.scroll(deltaX);
  }

  @EventListener(RefreshEvent.class)
  public final void refreshEvent(RefreshEvent e) {
    refresh(e.isForce());
  }

  @EventListener(UpEvent.class)
  public final void upEvent() {
    up();
  }

  @EventListener(DownEvent.class)
  public final void downEvent() {
    down();
  }

  @EventListener(LeftEvent.class)
  public final void leftEvent() {
    left();
  }

  @EventListener(RightEvent.class)
  public final void rightEvent() {
    right();
  }

  @EventListener(EscapeEvent.class)
  public final void escapeEvent() {
    escape();
  }

  @EventListener(ZoomEvent.class)
  public final void zoomEvent(ZoomEvent e) {
    zoom(e.getZoomFactor());
  }

  @EventListener(ScrollEvent.class)
  public final void scrollEvent(ScrollEvent e) {
    scroll(e.getDeltaX());
  }

  protected final GroupService<T, R, V> service() {
    return service;
  }

  private void changed() {
    Logger.getLogger(getClass().getName()).log(Level.FINE, axisXController::toString);
    int[][] chartData = service.read(axisXController.getStart(), axisXController.getEnd());
    FxUtils.invokeInFx(() -> {
      for (V v : service.getVariables()) {
        if (v.options().contains(Variable.Option.VISIBLE)) {
          int[] values = FilterBuilder.of().sharpingDecimate(axisXController.getDecimateFactor()).filter(chartData[v.ordinal()]);
          ScaleYInfo<V> scaleInfo = axisYController.scale(v, values);
          Objects.requireNonNull(chart).setAll(
              v.indexBy(Variable.Option.VISIBLE),
              IntStream.of(values).unordered().parallel().mapToDouble(scaleInfo).toArray(),
              scaleInfo
          );
        }
      }
      if (chartData[0].length > 0) {
        displayBanner(service.getVariables().stream()
            .mapToInt(
                e -> {
                  int[] ints = chartData[e.ordinal()];
                  return ints[ints.length - 1];
                })
            .toArray()
        );
      }
      axisXController.checkLength(chartData[0].length);
    });
  }

  private void displayBanner(int[] ints) {
    FxUtils.invokeInFx(() -> Objects.requireNonNull(chart).setBannerValues(
            service.getVariables().stream()
                .filter(v -> v.options().contains(Variable.Option.TEXT_VALUE_BANNER))
                .map(v -> "%,d".formatted(ints[v.ordinal()]))
                .collect(Collectors.joining(Strings.NEW_LINE_2))
        )
    );
  }
}