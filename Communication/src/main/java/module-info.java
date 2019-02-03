module com.ak.comm {
  requires com.ak.util;
  requires javax.inject;

  requires jsr305;
  requires jssc;
  requires java.logging;
  requires unit.api;

  exports com.ak.comm.bytes;
  exports com.ak.comm;
  exports com.ak.comm.converter;
  exports com.ak.comm.interceptor.simple;
  exports com.ak.comm.interceptor;
}