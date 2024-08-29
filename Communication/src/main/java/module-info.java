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

  exports com.ak.comm.core;
  exports com.ak.appliance.suntech.comm.converter;
  exports com.ak.appliance.suntech.comm.interceptor;
  exports com.ak.appliance.suntech.comm.bytes;
}