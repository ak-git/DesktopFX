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

import com.ak.comm.core.AbstractService;
import com.ak.comm.core.ConcurrentAsyncFileChannel;
import com.ak.comm.core.Refreshable;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.logging.LogBuilders;
import com.ak.util.Strings;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import static com.ak.comm.util.LogUtils.LOG_LEVEL_ERRORS;

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
  private final SerialPort serialPort = SerialPort.getCommPort(Ports.INSTANCE.next());
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
    return serialPort.isOpen();
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    synchronized (this) {
      int countBytes = 0;
      if (isOpen()) {
        src.rewind();
        try {
          byte[] bytes = new byte[src.remaining()];
          src.get(bytes);
          countBytes = serialPort.writeBytes(bytes, bytes.length);
        }
        catch (Exception ex) {
          LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
      return countBytes;
    }
  }

  @Override
  public void subscribe(Flow.Subscriber<? super ByteBuffer> s) {
    if (serialPort.getSystemPortName().isEmpty()) {
      LOGGER.log(Level.INFO, SERIAL_PORT_NOT_FOUND);
    }
    else {
      try {
        serialPort.openPort();
        serialPort.setComPortParameters(baudRate, 8, 1, 0);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        if (serialParams.contains(BytesInterceptor.SerialParams.RTS)) {
          serialPort.setRTS();
        }
        LOGGER.log(LOG_LEVEL_ERRORS, String.format("#%x Open port [ %s ], baudRate = %d bps", hashCode(), serialPort.getSystemPortName(), baudRate));
        s.onSubscribe(this);
        serialPort.addDataListener(new SerialPortDataListener() {
          @Override
          public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
          }

          @Override
          public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
              try {
                if (refresh) {
                  refresh = false;
                  s.onNext(ByteBuffer.allocate(0));
                  binaryLogChannel.close();
                }
                if (serialPort.bytesAvailable() > 0) {
                  buffer.clear();
                  byte[] bytes = new byte[serialPort.bytesAvailable()];
                  serialPort.readBytes(bytes, bytes.length);
                  buffer.put(bytes);
                  buffer.flip();
                  binaryLogChannel.write(buffer);
                  buffer.rewind();
                  logBytes(buffer);
                  s.onNext(buffer);
                }
              }
              catch (Exception ex) {
                logErrorAndComplete(s, ex);
              }
            }
          }
        });
      }
      catch (Exception ex) {
        logErrorAndComplete(s, ex);
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
      synchronized (this) {
        if (isOpen()) {
          LOGGER.log(LOG_LEVEL_ERRORS, "Close connection " + serialPort.getSystemPortName());
          serialPort.closePort();
        }
      }
    }
    catch (Exception ex) {
      LOGGER.log(LOG_LEVEL_ERRORS, serialPort.getSystemPortName(), ex);
    }
    binaryLogChannel.close();
  }

  @Override
  public void refresh() {
    LOGGER.log(Level.INFO, String.format("#%x Refresh connection [ %s ]", hashCode(), serialPort.getSystemPortName()));
    refresh = true;
  }

  @Override
  public String toString() {
    return String.format("%s@%x{serialPort = %s}", getClass().getSimpleName(), hashCode(), serialPort.getSystemPortName());
  }

  private void logErrorAndComplete(Flow.Subscriber<?> s, @Nonnull Exception ex) {
    try {
      LOGGER.log(LOG_LEVEL_ERRORS, serialPort.getSystemPortName(), ex);
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
      String[] portNames = Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).
          sorted(Comparator.comparingInt(usedPorts::indexOf)).toArray(String[]::new);
      if (portNames.length == 0) {
        return Strings.EMPTY;
      }
      else {
        String portName = portNames[0];
        LOGGER.log(LOG_LEVEL_ERRORS, String.format("Found { %s }, the [ %s ] is selected", Arrays.toString(portNames), portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return portName;
      }
    }
  }
}
