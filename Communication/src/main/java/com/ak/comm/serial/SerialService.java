package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.core.AbstractService;
import com.ak.comm.core.ConcurrentAsyncFileChannel;
import com.ak.comm.logging.LogBuilders;
import com.ak.util.Strings;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_ERRORS;

final class SerialService extends AbstractService implements WritableByteChannel, Publisher<ByteBuffer>, Refreshable {
  @Nonnull
  private final SerialPort serialPort;
  @Nonnegative
  private final int baudRate;
  @Nonnull
  private final ByteBuffer buffer;
  private final ConcurrentAsyncFileChannel binaryLogChannel = new ConcurrentAsyncFileChannel(() ->
      AsynchronousFileChannel.open(LogBuilders.SERIAL_BYTES.build(getClass().getSimpleName()).getPath(),
          StandardOpenOption.CREATE, StandardOpenOption.WRITE));
  private volatile boolean refresh;

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
      Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, "Serial port not found");
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
            if (refresh) {
              refresh = false;
              s.onNext(null);
              binaryLogChannel.close();
            }
            buffer.clear();
            buffer.put(serialPort.readBytes());
            buffer.flip();
            binaryLogChannel.write(buffer);
            buffer.rewind();
            logBytes(buffer);
            s.onNext(buffer);
          }
          catch (Exception ex) {
            logErrorAndComplete(s, ex);
          }
        }, SerialPort.MASK_RXCHAR);
      }
      catch (SerialPortException ex) {
        logErrorAndComplete(s, ex);
      }
    }
  }

  @Override
  public void close() {
    try {
      synchronized (serialPort) {
        if (serialPort.isOpened()) {
          Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, "Close connection " + serialPort.getPortName());
          serialPort.closePort();
        }
      }
    }
    catch (SerialPortException ex) {
      Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, serialPort.getPortName(), ex);
    }
    binaryLogChannel.close();
  }

  @Override
  public void refresh() {
    Logger.getLogger(getClass().getName()).log(Level.INFO,
        String.format("#%x Refresh connection [ %s ]", hashCode(), serialPort.getPortName()));
    refresh = true;
  }

  @Override
  public String toString() {
    return String.format("%s@%x{serialPort = %s}", getClass().getSimpleName(), hashCode(), serialPort.getPortName());
  }

  private void logErrorAndComplete(Subscriber<?> s, @Nonnull Exception ex) {
    try {
      Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS, serialPort.getPortName(), ex);
      close();
    }
    finally {
      s.onComplete();
    }
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
        Logger.getLogger(getClass().getName()).log(LOG_LEVEL_ERRORS,
            String.format("Found { %s }, the [ %s ] is selected", Arrays.toString(portNames), portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return portName;
      }
    }
  }
}
