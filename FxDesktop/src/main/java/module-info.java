module com.ak.fx.desktop {
  requires com.ak.comm;
  requires com.ak.util;
  requires com.ak.fx;

  requires uom.se;
  requires unit.api;
  requires java.logging;
  requires java.json;
  requires jsr305;
  requires javax.inject;

  requires javafx.fxml;
  requires javafx.controls;
  requires spring.context;
  requires spring.beans;

  requires java.sql;
  requires java.desktop;

  opens com.ak.fx.desktop to javafx.fxml;
  exports com.ak.fx.desktop;
}