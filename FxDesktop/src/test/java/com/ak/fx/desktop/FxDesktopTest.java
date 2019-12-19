package com.ak.fx.desktop;

import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.fx.stage.ScreenResolutionMonitor;
import com.ak.fx.storage.OSStageStorage;
import com.ak.fx.util.OSDockImage;
import com.ak.storage.Storage;
import com.ak.util.OS;
import com.ak.util.OSDirectory;
import com.ak.util.PropertiesSupport;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class FxDesktopTest extends Preloader {
  private static final Logger OS_DOCK_IMAGE_LOGGER = Logger.getLogger(OSDockImage.MAC.getClass().getName());
  private static final Logger FX_APP_LOGGER = Logger.getLogger(FxApplication.class.getName());
  private static final CountDownLatch LATCH = new CountDownLatch(2);
  private static final AtomicReference<Stage> STAGE_REFERENCE = new AtomicReference<>();
  private static final AtomicReference<Application> APP_REFERENCE = new AtomicReference<>();
  private static final double STAGE_X = 100.0;
  private static final double STAGE_Y = 100.0;
  private static final double STAGE_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2.0;
  private static final double STAGE_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2.0;

  private static final String JAVAFX_PRELOADER = "javafx.preloader";
  @Nullable
  private String oldPreloader;

  @BeforeClass(timeOut = 5000)
  public void setUp() throws InterruptedException {
    PropertiesSupport.OUT_CONVERTER_PATH.update(OSDirectory.VENDOR_ID);
    oldPreloader = System.getProperty(JAVAFX_PRELOADER);
    System.setProperty(JAVAFX_PRELOADER, FxDesktopTest.class.getName());

    cleanup();

    Executors.newSingleThreadExecutor().execute(() -> FxApplication.main(null));
    LATCH.await();
  }

  @AfterClass
  public void tearDown() {
    if (oldPreloader != null) {
      System.setProperty(JAVAFX_PRELOADER, oldPreloader);
    }

    cleanup();
  }

  private static void cleanup() {
    for (OSStageStorage storage : OSStageStorage.values()) {
      storage.newInstance(FxDesktopTest.class).delete();
    }
  }

  @Override
  public void start(@Nonnull Stage primaryStage) {
    STAGE_REFERENCE.set(primaryStage);
    setStageBounds(STAGE_X, STAGE_Y, STAGE_WIDTH, STAGE_HEIGHT);
    LATCH.countDown();
  }

  @Override
  public void handleStateChangeNotification(@Nonnull StateChangeNotification info) {
    if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
      Executors.newSingleThreadExecutor().execute(() -> {
        APP_REFERENCE.set(info.getApplication());
        LATCH.countDown();
      });
    }
  }

  @DataProvider(name = "os-storage")
  public static Object[][] newStorage() {
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
      try {
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
      }
      catch (Exception e) {
        Assert.fail(e.getMessage(), e);
      }
      finally {
        latch.countDown();
      }
    });
    latch.await();
    Assert.assertEquals(stage.isMaximized(), !maximizedFlag);
    Assert.assertEquals(stage.isFullScreen(), !fullScreenFlag);
  }

  @Test(dataProvider = "os-storage", expectedExceptions = UnsupportedOperationException.class)
  public void testGet(@Nonnull OSStageStorage storage) {
    storage.newInstance(getClass()).get();
  }

  @Test
  public static void testNames() {
    for (OS os : OS.values()) {
      OSStageStorage.valueOf(os.name());
      OSDockImage.valueOf(os.name());
    }
  }

  @Test
  public static void testInvalidSetIconImage() throws Exception {
    AtomicBoolean exceptionFlag = new AtomicBoolean(false);
    OS_DOCK_IMAGE_LOGGER.setFilter(record -> {
      Assert.assertNotNull(record.getThrown());
      exceptionFlag.set(true);
      return false;
    });
    OS_DOCK_IMAGE_LOGGER.setLevel(Level.CONFIG);

    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        for (OSDockImage dockImage : OSDockImage.values()) {
          dockImage.setIconImage(new Stage(), new URL("ftp://img.png/"));
        }
      }
      catch (MalformedURLException e) {
        Assert.fail(e.getMessage(), e);
      }
      finally {
        latch.countDown();
      }
    });
    latch.await();

    Assert.assertTrue(exceptionFlag.get(), "Exception must be thrown");
    OS_DOCK_IMAGE_LOGGER.setFilter(null);
    OS_DOCK_IMAGE_LOGGER.setLevel(Level.INFO);
  }

  @Test(expectedExceptions = {NullPointerException.class, IllegalArgumentException.class})
  public static void testInvalidApplicationStart() throws Exception {
    FX_APP_LOGGER.setLevel(Level.OFF);
    try {
      APP_REFERENCE.get().start(null);
    }
    finally {
      try {
        FX_APP_LOGGER.setLevel(Level.INFO);
      }
      catch (SecurityException e) {
        FX_APP_LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  @Test
  public static void testScreenResolutionMonitor() {
    try {
      ScreenResolutionMonitor.INSTANCE.dpi(() -> STAGE_REFERENCE.get().getScene());
    }
    catch (Exception e) {
      if (!STAGE_REFERENCE.get().isFullScreen()) {
        Assert.assertThrows(IllegalStateException.class, () -> {
        });
      }
    }
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
