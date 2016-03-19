package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public final class SingleSerialService extends AbstractSerialService {
  private final SerialPort serialPort;
  private final ByteBuffer buffer;

  public SingleSerialService(int baudRate) {
    buffer = ByteBuffer.allocate(baudRate);
    setPortName(Ports.INSTANCE.next());
    serialPort = new SerialPort(getPortName());
    if (getPortName().isEmpty()) {
      Logger.getLogger(getClass().getName()).config("Serial port not found");
    }
    else {
      try {
        serialPort.openPort();
        serialPort.setParams(baudRate, 8, 1, 0);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.addEventListener(event -> {
          try {
            buffer.clear();
            buffer.put(serialPort.readBytes());
            buffer.flip();
            bufferPublish().onNext(buffer);
          }
          catch (SerialPortException ex) {
            logAndClose(ex);
          }
        }, SerialPort.MASK_RXCHAR);
      }
      catch (SerialPortException ex) {
        logAndClose(ex);
      }
    }
  }

  @Override
  public boolean isWrite(byte[] bytes) {
    synchronized (this) {
      if (serialPort.isOpened()) {
        try {
          return serialPort.writeBytes(bytes);
        }
        catch (SerialPortException ex) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getPortName(), ex);
        }
      }
      return false;
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      try {
        if (serialPort.isOpened()) {
          serialPort.closePort();
        }
      }
      catch (SerialPortException ex) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, ex.getPortName(), ex);
      }
      finally {
        bufferPublish().onCompleted();
      }
    }
  }

  private void logAndClose(SerialPortException ex) {
    Logger.getLogger(getClass().getName()).log(Level.CONFIG, ex.getPortName(), ex);
    bufferPublish().onError(ex);
    close();
  }

  private enum Ports {
    INSTANCE;

    private final LinkedList<String> usedPorts = new LinkedList<>();

    public synchronized String next() {
      String[] portNames = SerialPortList.getPortNames((o1, o2) -> usedPorts.indexOf(o1) - usedPorts.indexOf(o2));
      if (portNames.length == 0) {
        return "";
      }
      else {
        String portName = portNames[0];
        Logger.getLogger(getClass().getName()).config(
            String.format("Found %s, the '%s' is selected", Arrays.toString(portNames), portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return portName;
      }
    }
  }
}
