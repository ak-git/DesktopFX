module com.ak.comm {
  requires com.ak.util;

  requires jsr305;
  requires com.fazecast.jSerialComm;
  requires java.logging;
  requires unit.api;
  requires uom.se;

  exports com.ak.comm;
  exports com.ak.comm.bytes;
  exports com.ak.comm.converter;
  exports com.ak.comm.interceptor;
  exports com.ak.comm.interceptor.simple;

  opens com.ak.comm.core to org.testng;
  opens com.ak.comm.file to org.testng;
  opens com.ak.comm.serial to org.testng;
  exports com.ak.comm.core;
}