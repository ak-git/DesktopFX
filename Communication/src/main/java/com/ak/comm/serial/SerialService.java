package com.ak.comm.serial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.core.AbstractService;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

final class SerialService extends AbstractService<ByteBuffer> implements WritableByteChannel {
  private final SerialPort serialPort;
  private final ByteBuffer buffer;
  private WritableByteChannel binaryLogChannel;

  SerialService(String name, int baudRate) {
    buffer = ByteBuffer.allocate(baudRate);
    String portName = Ports.INSTANCE.next();
    serialPort = new SerialPort(portName);
    if (portName.isEmpty()) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Serial port not found");
    }
    else {
      try {
        serialPort.openPort();
        serialPort.setParams(baudRate, 8, 1, 0);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        Logger.getLogger(getClass().getName()).log(Level.INFO, String.format("Open port [ %s ], baudRate = %d bps", this, baudRate));
        serialPort.addEventListener(event -> {
          if (binaryLogChannel == null) {
            try {
              Path path = new BinaryLogBuilder(name, LocalFileHandler.class).build().getPath();
              binaryLogChannel = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
              Logger.getLogger(getClass().getName()).log(Level.INFO,
                  String.format("Bytes from serial port are logging into the file [ %s ]", path));
            }
            catch (IOException ex) {
              logAndClose(Level.WARNING, serialPort.getPortName(), ex);
            }
          }

          try {
            buffer.clear();
            buffer.put(serialPort.readBytes());
            buffer.flip();
            binaryLogChannel.write(buffer);
            bufferPublish().onNext(buffer);
          }
          catch (Exception ex) {
            logAndClose(Level.CONFIG, serialPort.getPortName(), ex);
          }
        }, SerialPort.MASK_RXCHAR);
      }
      catch (SerialPortException ex) {
        logAndClose(Level.CONFIG, serialPort.getPortName(), ex);
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
      if (serialPort.isOpened()) {
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
      try {
        synchronized (serialPort) {
          if (serialPort.isOpened()) {
            serialPort.closePort();
          }
        }
      }
      catch (SerialPortException ex) {
        Logger.getLogger(getClass().getName()).log(Level.CONFIG, serialPort.getPortName(), ex);
      }

      try {
        if (binaryLogChannel != null) {
          binaryLogChannel.close();
        }
      }
      catch (IOException ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, serialPort.getPortName(), ex);
      }
    }
    finally {
      bufferPublish().onCompleted();
    }
  }

  @Override
  public String toString() {
    return String.format("%s@%x{serialPort = %s}", getClass().getSimpleName(), hashCode(), serialPort.getPortName());
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
        Logger.getLogger(getClass().getName()).log(Level.CONFIG,
            String.format("Found { %s }, the [ %s ] is selected", Arrays.toString(portNames), portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return portName;
      }
    }
  }
}
