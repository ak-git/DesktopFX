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
  requires spring.boot;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.core;

  requires java.prefs;
  requires java.desktop;
  requires spring.beans;

  opens com.ak.numbers.aper to com.ak.util, org.testng;
  opens com.ak.numbers.rcm to com.ak.util, org.testng;
  opens com.ak.numbers.common to org.testng;
  opens com.ak.fx.desktop.suntech to javafx.fxml;
  opens com.ak.fx.desktop to javafx.fxml, spring.core;
  opens com.ak.fx.scene to javafx.fxml, org.testng;
  opens com.ak.comm.interceptor.nmis to spring.beans, org.testng;
  opens com.ak.comm.interceptor.nmisr to org.testng;
  opens com.ak.comm.interceptor.rcm to spring.beans, org.testng;
  opens com.ak.comm.interceptor.rsce to org.testng;
  opens com.ak.comm.converter.rsce to org.testng;
  opens com.ak.comm.converter.nmis to com.ak.comm, org.testng;
  opens com.ak.comm.converter.aper to com.ak.comm, org.testng;

  exports com.ak.comm.interceptor.suntech to org.testng;
  exports com.ak.comm.converter.aper to javafx.graphics, org.testng;
  exports com.ak.comm.converter.rcm to javafx.graphics, org.testng;
  exports com.ak.comm.converter.rsce to spring.beans;
  exports com.ak.comm.converter.nmis to spring.beans;
  exports com.ak.comm.converter.briko to org.testng;
  exports com.ak.comm.converter.suntech to org.testng;
  exports com.ak.comm.bytes.suntech to org.testng;

  exports com.ak.fx.desktop.briko to spring.beans;
  exports com.ak.fx.desktop.nmisr to spring.beans;
  exports com.ak.fx.desktop.suntech to spring.beans;

  exports com.ak.comm.bytes.nmis;
  exports com.ak.comm.bytes.rsce;
  exports com.ak.fx.desktop;
}