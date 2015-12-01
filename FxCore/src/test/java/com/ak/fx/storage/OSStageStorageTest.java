package com.ak.fx.storage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.ak.storage.Storage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class OSStageStorageTest extends Application {
  private static final CountDownLatch LATCH = new CountDownLatch(1);
  private static final AtomicReference<Stage> STAGE_REFERENCE = new AtomicReference<>();
  private static final double STAGE_X = 10.0;
  private static final double STAGE_Y = 20.0;
  private static final double STAGE_WIDTH = 800.0;
  private static final double STAGE_HEIGHT = 600.0;

  @BeforeClass
  public void setUp() throws InterruptedException {
    Executors.newSingleThreadExecutor().execute(() -> Application.launch(OSStageStorageTest.class));
    LATCH.await();
    cleanup();
  }

  @AfterClass
  public void cleanup() {
    for (OSStageStorage storage : OSStageStorage.values()) {
      storage.newInstance(getClass()).delete();
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    LATCH.countDown();
    STAGE_REFERENCE.set(primaryStage);
    setStageBounds(STAGE_X, STAGE_Y, STAGE_WIDTH, STAGE_HEIGHT);
  }

  @DataProvider(name = "os-storage")
  public static Object[][] newInstance() {
    Object[][] result = new Object[OSStageStorage.values().length][1];
    OSStageStorage[] values = OSStageStorage.values();
    for (int i = 0; i < values.length; i++) {
      result[i][0] = values[i];
    }
    return result;
  }

  @Test(dataProvider = "os-storage")
  public void testBoundsStorage(OSStageStorage storage) throws Exception {
    Storage<Stage> stageStorage = storage.newInstance(getClass());
    Stage stage = STAGE_REFERENCE.get();

    boolean maximizedFlag = stage.isMaximized();
    boolean fullScreenFlag = stage.isFullScreen();
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      stageStorage.update(stage);
      checkStage();

      stageStorage.save(stage);
      setStageBounds(STAGE_X / 2.0, STAGE_Y / 2.0, STAGE_WIDTH / 2.0, STAGE_HEIGHT / 2.0);
      stageStorage.update(stage);
      checkStage();

      stage.setMaximized(!maximizedFlag);
      stage.setFullScreen(!fullScreenFlag);
      if (storage == OSStageStorage.MAC) {
        try {
          TimeUnit.SECONDS.sleep(4);
        }
        catch (InterruptedException e) {
          Assert.fail(e.getMessage(), e);
        }
      }
      stageStorage.save(stage);
      stageStorage.update(stage);
      latch.countDown();
    });
    latch.await();
    Assert.assertEquals(stage.isMaximized(), !maximizedFlag);
    Assert.assertEquals(stage.isFullScreen(), !fullScreenFlag);
  }

  @Test(dataProvider = "os-storage", expectedExceptions = UnsupportedOperationException.class)
  public void testGet(OSStageStorage storage) {
    storage.newInstance(getClass()).get();
  }

  private static void setStageBounds(double x, double y, double width, double height) {
    Stage stage = STAGE_REFERENCE.get();
    stage.setX(x);
    stage.setY(y);
    stage.setWidth(width);
    stage.setHeight(height);
  }

  private static void checkStage() {
    Stage stage = STAGE_REFERENCE.get();
    Assert.assertEquals(stage.getX(), STAGE_X, Float.MIN_NORMAL);
    Assert.assertEquals(stage.getY(), STAGE_Y, Float.MIN_NORMAL);
    Assert.assertEquals(stage.getWidth(), STAGE_WIDTH, Float.MIN_NORMAL);
    Assert.assertEquals(stage.getHeight(), STAGE_HEIGHT, Float.MIN_NORMAL);
  }
}
