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

  opens com.ak.numbers.aper to com.ak.util;
  opens com.ak.numbers.rcm to com.ak.util;
  exports com.ak.comm.converter.rcm to spring.beans;
  exports com.ak.comm.interceptor.rcm to spring.beans;
  exports com.ak.comm.converter.aper to javafx.graphics;
  opens com.ak.fx.desktop to javafx.fxml;
  opens com.ak.fx.scene to javafx.fxml;
  exports com.ak.fx.desktop;
}