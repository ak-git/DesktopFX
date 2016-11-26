package com.ak.fx.stage;

import java.awt.Toolkit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import com.ak.util.UIConstants;
import com.sun.javafx.util.Utils;
import io.reactivex.Observable;
import javafx.stage.Stage;

import static java.util.concurrent.TimeUnit.SECONDS;

@Immutable
@ThreadSafe
public enum ScreenResolutionMonitor {
  INSTANCE;

  private final AtomicReference<Double> dpi = new AtomicReference<>(Toolkit.getDefaultToolkit().getScreenResolution() * 1.0);
  private final AtomicReference<Stage> stage = new AtomicReference<>();
  private final Observable<Double> dpiObservable = Observable.merge(
      Observable.create(subscriber -> subscriber.onNext(getDpi())),
      Observable.interval(0, UIConstants.UI_DELAY.getSeconds(), SECONDS).
          skipWhile(index -> stage.get() == null).map(index -> stage.get()).map(stage -> Utils.getScreen(stage).getDpi())).
      distinctUntilChanged().
      doOnNext(dpi -> {
        this.dpi.set(dpi);
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, String.format("Screen resolution is %.0f dpi", dpi));
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
