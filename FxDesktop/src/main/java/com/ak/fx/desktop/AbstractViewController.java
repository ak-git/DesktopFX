package com.ak.fx.desktop;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.inject.Inject;

import com.ak.comm.GroupService;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public abstract class AbstractViewController<RESPONSE, REQUEST, EV extends Enum<EV> & Variable<EV>> implements Initializable {
  @Nonnull
  private final GroupService<RESPONSE, REQUEST, EV> service;
  @Nonnull
  private final boolean[] displayVariables;
  @Nonnull
  @FXML
  private Pane root = new Pane();

  @Inject
  public AbstractViewController(@Nonnull GroupService<RESPONSE, REQUEST, EV> service) {
    this.service = service;
    displayVariables = new boolean[service.getVariables().size()];
    for (int i = 0; i < displayVariables.length; i++) {
      displayVariables[i] = Variables.isDisplay(service.getVariables().get(i));
    }
  }

  @OverridingMethodsMustInvokeSuper
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

  protected final Pane root() {
    return root;
  }

  protected final boolean isDisplayed(@Nonnegative int index) {
    return displayVariables[index];
  }

  protected final GroupService<RESPONSE, REQUEST, EV> service() {
    return service;
  }
}
