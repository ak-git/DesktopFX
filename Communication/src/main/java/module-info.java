module com.ak.comm {
  requires com.ak.util;

  requires com.fazecast.jSerialComm;
  requires java.logging;
  requires unit.api;
  requires uom.se;
  requires jsr305;
  requires org.jspecify;

  exports com.ak.comm;
  exports com.ak.comm.bytes;
  exports com.ak.comm.converter;
  exports com.ak.comm.interceptor;
  exports com.ak.comm.interceptor.simple;

  exports com.ak.appliance.briko.comm.converter;
  exports com.ak.appliance.briko.comm.interceptor;
  exports com.ak.appliance.kleiber.comm.converter;
  exports com.ak.appliance.kleiber.comm.interceptor;
  exports com.ak.appliance.nmi.comm.converter;
  exports com.ak.appliance.nmis.comm.bytes;
  exports com.ak.appliance.nmis.comm.converter;
  exports com.ak.appliance.nmis.comm.interceptor;
  exports com.ak.appliance.purelogic.comm.bytes;
  exports com.ak.appliance.purelogic.comm.converter;
  exports com.ak.appliance.purelogic.comm.interceptor;
  exports com.ak.appliance.rsce.comm.bytes;
  exports com.ak.appliance.rsce.comm.converter;
  exports com.ak.appliance.rsce.comm.interceptor;
  exports com.ak.appliance.sktbpr.comm.bytes;
  exports com.ak.appliance.sktbpr.comm.converter;
  exports com.ak.appliance.sktbpr.comm.interceptor;
  exports com.ak.appliance.suntech.comm.bytes;
  exports com.ak.appliance.suntech.comm.converter;
  exports com.ak.appliance.suntech.comm.interceptor;
  exports com.ak.comm.core;
}