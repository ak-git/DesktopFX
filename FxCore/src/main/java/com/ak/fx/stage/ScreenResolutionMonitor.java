package com.ak.fx.stage;

import java.awt.Toolkit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.ak.util.UIConstants;
import com.sun.javafx.util.Utils;
import javafx.stage.Stage;
import rx.Observable;

import static java.util.concurrent.TimeUnit.SECONDS;

public enum ScreenResolutionMonitor {
  INSTANCE;

  private final AtomicReference<Double> dpi = new AtomicReference<>(Toolkit.getDefaultToolkit().getScreenResolution() * 1.0);
  private final AtomicReference<Stage> stage = new AtomicReference<>();
  private final Observable<Double> dpiObservable = Observable.merge(
      Observable.create(subscriber -> subscriber.onNext(getDpi())),
      Observable.interval(0, UIConstants.UI_DELAY.getSeconds(), SECONDS).
          map(index -> stage.get()).skipWhile(stage -> stage == null).map(stage -> Utils.getScreen(stage).getDpi())).
      distinctUntilChanged().
      doOnNext(dpi -> {
        this.dpi.set(dpi);
        Logger.getLogger(getClass().getName()).config(String.format("Screen resolution is %.0f dpi", dpi));
      });

  public static void setStage(Stage stage) {
    if (!INSTANCE.stage.compareAndSet(null, Objects.requireNonNull(stage))) {
      throw new IllegalStateException(
          String.format("Stage %s was already initialized, new stage %s is ignored", INSTANCE.stage.get(), stage));
    }
  }

  public Observable<Double> getDpiObservable() {
    return dpiObservable;
  }

  public double getDpi() {
    return dpi.get();
  }
}
