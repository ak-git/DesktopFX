package com.ak.fx.desktop;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.fx.scene.MilliGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public final class ViewController implements Initializable {
  @Nullable
  @FXML
  public MilliGrid root;

  @Override
  public void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
  }
}
