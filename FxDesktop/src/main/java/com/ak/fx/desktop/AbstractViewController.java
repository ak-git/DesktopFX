package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;
import com.ak.fx.scene.Chart;
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
  @Nullable
  private Flow.Subscription subscription;
  @Nullable
  @FXML
  private Chart<EV> chart;

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
      chart.init(service);
    }
    service.subscribe(this);
  }

  @Override
  public final void onSubscribe(@Nonnull Flow.Subscription s) {
    if (subscription != null) {
      subscription.cancel();
    }
    subscription = s;
    subscription.request(Objects.requireNonNull(chart).getAxisXController().getLength());
    onComplete();
  }

  @Override
  public final void onNext(@Nonnull int[] ints) {
    Objects.requireNonNull(chart).add(ints);
  }

  @Override
  public final void onError(@Nonnull Throwable t) {
    Logger.getLogger(getClass().getName()).log(Level.WARNING, t.getMessage(), t);
  }

  @Override
  public final void onComplete() {
    Objects.requireNonNull(chart).changed();
  }
}