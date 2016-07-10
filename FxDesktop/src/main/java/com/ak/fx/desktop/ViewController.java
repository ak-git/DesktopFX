package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.ak.comm.file.AutoFileReadingService;
import com.ak.fx.scene.MilliGrid;
import com.ak.hardware.nmis.comm.interceptor.NmisRequest;
import com.ak.hardware.nmis.comm.interceptor.NmisResponseFrame;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public final class ViewController implements Initializable {
  @Nonnull
  @FXML
  public MilliGrid root = new MilliGrid();
  @Nonnull
  private final AutoFileReadingService<NmisResponseFrame, NmisRequest> service;

  @Inject
  public ViewController(@Nonnull AutoFileReadingService<NmisResponseFrame, NmisRequest> service) {
    this.service = service;
  }

  @Override
  public void initialize(@Nonnull URL location, @Nonnull ResourceBundle resources) {
    root.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (db.hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
      }
      else {
        event.consume();
      }
    });
    root.setOnDragDropped(event -> {
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
  }
}
