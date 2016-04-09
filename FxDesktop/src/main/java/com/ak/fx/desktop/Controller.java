package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.ak.comm.file.AutoFileService;
import com.ak.fx.scene.MilliGrid;
import com.ak.hardware.tnmi.comm.interceptor.TnmiRequest;
import com.ak.hardware.tnmi.comm.interceptor.TnmiResponse;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public final class Controller implements Initializable {
  public MilliGrid root;
  private final AutoFileService<TnmiResponse, TnmiRequest> service;

  @Inject
  public Controller(AutoFileService<TnmiResponse, TnmiRequest> service) {
    this.service = service;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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
      if (db.hasFiles()) {
        for (File file : db.getFiles()) {
          if (file.isFile()) {
            service.open(file.toPath());
            break;
          }
        }
      }
      event.setDropCompleted(db.hasFiles());
      event.consume();
    });
  }
}
