package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.comm.converter.Refreshable;
import com.ak.comm.core.AbstractService;
import com.ak.comm.core.ConcurrentAsyncFileChannel;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.util.Strings;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import static com.ak.util.LogUtils.LOG_LEVEL_ERRORS;

final class SerialService extends AbstractService implements WritableByteChannel, Flow.Publisher<ByteBuffer>, Refreshable, Flow.Subscription {
  private static final Logger LOGGER = Logger.getLogger(SerialService.class.getName());
  private static final String SERIAL_PORT_NOT_FOUND = "Serial port not found";

  static {
    LOGGER.setFilter(new Filter() {
      private boolean notFoundFlag;

      @Override
      public boolean isLoggable(LogRecord record) {
        if (SERIAL_PORT_NOT_FOUND.equals(record.getMessage())) {
          if (notFoundFlag) {
            return false;
          }
          else {
            notFoundFlag = true;
            return true;
          }
        }
        else {
          notFoundFlag = false;
          return true;
        }
      }
    });
  }

  @Nonnull
  private final SerialPort serialPort = new SerialPort(Ports.INSTANCE.next());
  @Nonnegative
  private final int baudRate;
  @Nonnull
  private final ByteBuffer buffer;
  @Nonnull
  private final Set<BytesInterceptor.SerialParams> serialParams;
  private final ConcurrentAsyncFileChannel binaryLogChannel = new ConcurrentAsyncFileChannel(() ->
      AsynchronousFileChannel.open(LogBuilders.SERIAL_BYTES.build(getClass().getSimpleName()).getPath(),
          StandardOpenOption.CREATE, StandardOpenOption.WRITE));
  private volatile boolean refresh;

  SerialService(@Nonnegative int baudRate, Set<BytesInterceptor.SerialParams> serialParams) {
    this.baudRate = baudRate;
    buffer = ByteBuffer.allocate(baudRate);
    this.serialParams = Collections.unmodifiableSet(serialParams);
  }

  @Override
  public boolean isOpen() {
    return serialPort.isOpened();
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
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
          LOGGER.log(Level.WARNING, ex.getPortName(), ex);
        }
      }
      return countBytes;
    }
  }

  @Override
  public void subscribe(Flow.Subscriber<? super ByteBuffer> s) {
    if (serialPort.getPortName().isEmpty()) {
      LOGGER.log(Level.INFO, SERIAL_PORT_NOT_FOUND);
    }
    else {
      try {
        serialPort.openPort();
        serialPort.setParams(baudRate, 8, 1, 0);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        if (serialParams.contains(BytesInterceptor.SerialParams.CLEAR_DTR)) {
          serialPort.setDTR(false);
        }
        LOGGER.log(LOG_LEVEL_ERRORS, () -> String.format("#%x Open port [ %s ], baudRate = %d bps", hashCode(), serialPort.getPortName(), baudRate));
        s.onSubscribe(this);
        serialPort.addEventListener(event -> {
          try {
            if (refresh) {
              refresh = false;
              s.onNext(ByteBuffer.allocate(0));
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
  public void request(long n) {
    if (n < 1) {
      cancel();
    }
  }

  @Override
  public void cancel() {
    close();
  }

  @Override
  public void close() {
    try {
      synchronized (serialPort) {
        if (isOpen()) {
          LOGGER.log(LOG_LEVEL_ERRORS, "Close connection " + serialPort.getPortName());
          serialPort.closePort();
        }
      }
    }
    catch (SerialPortException ex) {
      LOGGER.log(LOG_LEVEL_ERRORS, serialPort.getPortName(), ex);
    }
    binaryLogChannel.close();
  }

  @Override
  public void refresh() {
    LOGGER.log(Level.INFO, () -> String.format("#%x Refresh connection [ %s ]", hashCode(), serialPort.getPortName()));
    refresh = true;
  }

  @Override
  public String toString() {
    return String.format("%s@%x{serialPort = %s}", getClass().getSimpleName(), hashCode(), serialPort.getPortName());
  }

  private void logErrorAndComplete(Flow.Subscriber<?> s, @Nonnull Exception ex) {
    try {
      LOGGER.log(LOG_LEVEL_ERRORS, serialPort.getPortName(), ex);
      close();
    }
    finally {
      s.onComplete();
    }
  }

  private enum Ports {
    INSTANCE;

    private final LinkedList<String> usedPorts = new LinkedList<>();

    synchronized String next() {
      String[] portNames = SerialPortList.getPortNames(Comparator.comparingInt(usedPorts::indexOf));
      if (portNames.length == 0) {
        return Strings.EMPTY;
      }
      else {
        String portName = portNames[0];
        LOGGER.log(LOG_LEVEL_ERRORS, () -> String.format("Found { %s }, the [ %s ] is selected", Arrays.toString(portNames), portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return portName;
      }
    }
  }
}
