module com.ak.comm {
  requires com.ak.util;
  requires javax.inject;

  requires jsr305;
  requires jssc;
  requires java.logging;
  requires unit.api;
  requires uom.se;

  exports com.ak.comm.bytes;
  exports com.ak.comm;
  exports com.ak.comm.converter;
  exports com.ak.comm.interceptor.simple;
  exports com.ak.comm.interceptor;

  opens com.ak.comm.core to org.testng;
  opens com.ak.comm.converter to org.testng;
  opens com.ak.comm.file to org.testng;
  opens com.ak.comm to org.testng;
  opens com.ak.comm.interceptor.simple to org.testng;
  opens com.ak.comm.logging to org.testng;
  opens com.ak.comm.serial to org.testng;
}