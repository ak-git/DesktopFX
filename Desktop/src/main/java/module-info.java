module com.ak.fx.desktop {
  requires com.ak.comm;
  requires com.ak.util;
  requires com.ak.fx;

  requires commons.math3;
  requires uom.se;
  requires unit.api;
  requires java.logging;
  requires java.json;
  requires jsr305;
  requires org.jspecify;
  requires jakarta.inject;

  requires javafx.fxml;
  requires javafx.controls;
  requires spring.boot;
  requires spring.boot.autoconfigure;
  requires spring.context;
  requires spring.core;

  requires spring.beans;
  requires java.desktop;

  opens com.ak.appliance.aper.numbers to com.ak.util;
  opens com.ak.appliance.rcm.numbers to com.ak.util;
  opens com.ak.appliance.suntech.fx.desktop to javafx.fxml;
  opens com.ak.fx.desktop to javafx.fxml, spring.core;
  opens com.ak.appliance.sktbpr.fx.desktop to spring.core;
  opens com.ak.appliance.nmisr.fx.desktop to spring.core;
  opens com.ak.fx.scene to javafx.fxml;
  opens com.ak.appliance.nmis.comm.interceptor to spring.beans;
  opens com.ak.appliance.rcm.comm.interceptor to spring.beans;
  opens com.ak.spring to spring.core;

  // variables i18n
  opens com.ak.appliance.aper.comm.converter to com.ak.comm;
  opens com.ak.appliance.nmis.comm.converter to com.ak.comm;
  opens com.ak.appliance.rcm.comm.converter to com.ak.comm;

  exports com.ak.appliance.aper.comm.converter to javafx.graphics;
  exports com.ak.appliance.rcm.comm.converter to javafx.graphics;
  exports com.ak.appliance.nmis.comm.converter to spring.beans;

  exports com.ak.appliance.briko.fx.desktop to spring.beans;
  exports com.ak.appliance.nmisr.fx.desktop to spring.beans;
  exports com.ak.appliance.purelogic.fx.desktop to spring.beans;
  exports com.ak.appliance.sktbpr.fx.desktop to spring.beans, spring.context;
  exports com.ak.appliance.suntech.fx.desktop to spring.beans;
  exports com.ak.spring to javafx.graphics, spring.beans, spring.context;

  exports com.ak.appliance.nmis.comm.bytes;
  exports com.ak.fx.desktop;
  opens com.ak.fx to javafx.fxml, spring.core;
  exports com.ak.fx;
}