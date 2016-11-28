package com.ak.comm.serial;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.core.AbstractService;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.BinaryLogBuilder;
import com.ak.logging.LocalFileHandler;
import com.ak.util.Strings;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.reactivestreams.Subscriber;

final class SerialService extends AbstractService implements WritableByteChannel {
  @Nonnull
  private final BytesInterceptor<?, ?> interceptor;
  @Nonnull
  private final SerialPort serialPort;
  @Nonnull
  private final ByteBuffer buffer;
  @Nullable
  private WritableByteChannel binaryLogChannel;

  SerialService(@Nonnull BytesInterceptor<?, ?> interceptor) {
    this.interceptor = interceptor;
    buffer = ByteBuffer.allocate(interceptor.getBaudRate());
    String portName = Ports.INSTANCE.next();
    serialPort = new SerialPort(portName);
    if (portName.isEmpty()) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Serial port not found");
    }
  }

  @Override
  public boolean isOpen() {
    return serialPort.isOpened();
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
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
  public void subscribe(Subscriber<? super ByteBuffer> s) {
    try {
      serialPort.openPort();
      serialPort.setParams(interceptor.getBaudRate(), 8, 1, 0);
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
      Logger.getLogger(getClass().getName()).log(Level.INFO,
          String.format("#%x Open port [ %s ], baudRate = %d bps", hashCode(), serialPort.getPortName(), interceptor.getBaudRate()));
      s.onSubscribe(this);
      serialPort.addEventListener(event -> {
        if (binaryLogChannel == null) {
          try {
            Path path = new BinaryLogBuilder(interceptor.name(), LocalFileHandler.class).build().getPath();
            binaryLogChannel = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                String.format("#%x Bytes from port [ %s ] are logging into the file [ %s ]", hashCode(), serialPort.getPortName(), path));
          }
          catch (IOException ex) {
            logErrorAndClose(s, Level.WARNING, serialPort.getPortName(), ex);
          }
        }

        try {
          buffer.clear();
          buffer.put(serialPort.readBytes());
          buffer.flip();
          binaryLogChannel.write(buffer);
          logBytes(buffer);
          s.onNext(buffer);
        }
        catch (Exception ex) {
          logErrorAndClose(s, Level.CONFIG, serialPort.getPortName(), ex);
        }
      }, SerialPort.MASK_RXCHAR);
    }
    catch (SerialPortException ex) {
      logErrorAndClose(s, Level.CONFIG, serialPort.getPortName(), ex);
    }
  }

  @Override
  public void request(long n) {
  }

  @Override
  public void cancel() {
    close();
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
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Close connection " + serialPort.getPortName());
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
      String[] portNames = SerialPortList.getPortNames(Comparator.comparingInt(usedPorts::indexOf));
      if (portNames.length == 0) {
        return Strings.EMPTY;
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
