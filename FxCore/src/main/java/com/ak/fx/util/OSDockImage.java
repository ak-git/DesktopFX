package com.ak.fx.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public enum OSDockImage {
  WINDOWS,
  MAC {
    @Override
    public void setIconImage(Stage stage, URL imageURL) {
      super.setIconImage(stage, imageURL);
      try {
        callApplicationMethod("setDockIconImage", java.awt.Image.class, ImageIO.read(imageURL));
      }
      catch (IOException e) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, e.getMessage(), e);
      }
    }

    private <T> void callApplicationMethod(String methodName, Class<? super T> type, T value) {
      try {
        Class<?> clazz = Class.forName("com.apple.eawt.Application");
        Method method = clazz.getMethod("getApplication");
        Method method2 = clazz.getMethod(methodName, type);
        method2.invoke(method.invoke(null), value);
      }
      catch (Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage(), ex);
      }
    }
  },
  UNIX;

  public void setIconImage(Stage stage, URL imageURL) {
    stage.getIcons().add(new Image(imageURL.toString()));
  }
}

