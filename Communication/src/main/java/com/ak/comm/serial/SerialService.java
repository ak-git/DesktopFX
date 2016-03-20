package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public final class SerialService extends AbstractService<ByteBuffer> implements WritableByteChannel {
  private final SerialPort serialPort;
  private final ByteBuffer buffer;

  public SerialService(int baudRate) {
    buffer = ByteBuffer.allocate(baudRate);
    String portName = Ports.INSTANCE.next();
    serialPort = new SerialPort(portName);
    if (portName.isEmpty()) {
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
          catch (Exception ex) {
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
  public boolean isOpen() {
    return serialPort.isOpened();
  }

  @Override
  public int write(ByteBuffer src) {
    synchronized (serialPort) {
      int countBytes = 0;
      if (isOpen()) {
        src.rewind();
        try {
          while (src.hasRemaining()) {
            if (serialPort.writeByte(src.get())) {
              countBytes++;
            }
          }
        }
        catch (SerialPortException ex) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getPortName(), ex);
        }
      }
      return countBytes;
    }
  }

  @Override
  public void close() {
    try {
      synchronized (serialPort) {
        if (serialPort.isOpened()) {
          serialPort.closePort();
        }
      }
    }
    catch (SerialPortException ex) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, ex.getPortName(), ex);
    }
    finally {
      bufferPublish().onCompleted();
    }
  }

  @Override
  public String toString() {
    return String.format("%s@%s, port = [ %s ]", getClass().getSimpleName(), Integer.toHexString(hashCode()), serialPort.getPortName());
  }

  private void logAndClose(Exception ex) {
    Logger.getLogger(getClass().getName()).log(Level.CONFIG, serialPort.getPortName(), ex);
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
