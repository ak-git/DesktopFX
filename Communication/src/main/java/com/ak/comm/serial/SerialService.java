package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.core.AbstractService;
import com.ak.logging.SafeByteChannel;
import com.ak.util.Strings;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.reactivestreams.Subscriber;

final class SerialService extends AbstractService<ByteBuffer> implements WritableByteChannel {
  @Nonnull
  private final SerialPort serialPort;
  @Nonnegative
  private final int baudRate;
  @Nonnull
  private final ByteBuffer buffer;
  private final SafeByteChannel binaryLogChannel = new SafeByteChannel(getClass().getSimpleName());

  SerialService(@Nonnegative int baudRate) {
    this.baudRate = baudRate;
    buffer = ByteBuffer.allocate(baudRate);
    serialPort = new SerialPort(Ports.INSTANCE.next());
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
    if (serialPort.getPortName().isEmpty()) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Serial port not found");
    }
    else {
      try {
        serialPort.openPort();
        serialPort.setParams(baudRate, 8, 1, 0);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        Logger.getLogger(getClass().getName()).log(Level.INFO,
            String.format("#%x Open port [ %s ], baudRate = %d bps", hashCode(), serialPort.getPortName(), baudRate));
        s.onSubscribe(this);
        serialPort.addEventListener(event -> {
          try {
            buffer.clear();
            buffer.put(serialPort.readBytes());
            buffer.flip();
            binaryLogChannel.write(buffer);
            logBytes(buffer);
            s.onNext(buffer);
          }
          catch (Exception ex) {
            logErrorAndComplete(s, Level.CONFIG, ex);
          }
        }, SerialPort.MASK_RXCHAR);
      }
      catch (SerialPortException ex) {
        logErrorAndComplete(s, Level.CONFIG, ex);
      }
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
      synchronized (serialPort) {
        if (serialPort.isOpened()) {
          Logger.getLogger(getClass().getName()).log(Level.CONFIG, "Close connection " + serialPort.getPortName());
          serialPort.closePort();
        }
      }
    }
    catch (SerialPortException ex) {
      Logger.getLogger(getClass().getName()).log(Level.CONFIG, serialPort.getPortName(), ex);
    }
    binaryLogChannel.close();
  }

  @Override
  public String toString() {
    return String.format("%s@%x{serialPort = %s}", getClass().getSimpleName(), hashCode(), serialPort.getPortName());
  }

  private void logErrorAndComplete(Subscriber<?> s, @Nonnull Level level, @Nonnull Exception ex) {
    Logger.getLogger(getClass().getName()).log(level, serialPort.getPortName(), ex);
    cancel();
    s.onComplete();
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
