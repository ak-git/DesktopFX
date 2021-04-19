package com.ak.comm.serial;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.Flow;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.comm.core.AbstractService;
import com.ak.comm.core.ConcurrentAsyncFileChannel;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;

final class SerialService<T, R> extends AbstractService<ByteBuffer> implements WritableByteChannel, Flow.Subscription {
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

  @Nullable
  private final SerialPort serialPort = Ports.INSTANCE.next();
  @Nonnull
  private final BytesInterceptor<T, R> bytesInterceptor;
  @Nonnull
  private final ByteBuffer buffer;
  private final ConcurrentAsyncFileChannel binaryLogChannel;
  private volatile boolean refresh;

  SerialService(@Nonnull BytesInterceptor<T, R> bytesInterceptor) {
    this.bytesInterceptor = bytesInterceptor;
    buffer = ByteBuffer.allocate(bytesInterceptor.getBaudRate());
    binaryLogChannel = new ConcurrentAsyncFileChannel(() ->
        AsynchronousFileChannel.open(LogBuilders.SERIAL_BYTES.build(bytesInterceptor.name()).getPath(),
            StandardOpenOption.CREATE, StandardOpenOption.WRITE));
  }

  @Override
  public boolean isOpen() {
    return serialPort != null && serialPort.isOpen();
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    synchronized (this) {
      int countBytes = 0;
      if (isOpen() && serialPort != null) {
        src.rewind();
        countBytes = serialPort.writeBytes(src.array(), src.limit());
      }
      return countBytes;
    }
  }

  @Override
  public void subscribe(Flow.Subscriber<? super ByteBuffer> s) {
    if (serialPort == null) {
      LOGGER.log(Level.INFO, SERIAL_PORT_NOT_FOUND);
    }
    else {
      serialPort.openPort();
      serialPort.setBaudRate(bytesInterceptor.getBaudRate());
      if (bytesInterceptor.getSerialParams().contains(BytesInterceptor.SerialParams.CLEAR_DTR)) {
        serialPort.clearDTR();
      }
      LOGGER.log(Level.INFO, () -> "%s Open port, baudRate = %d bps".formatted(this, bytesInterceptor.getBaudRate()));
      s.onSubscribe(this);

      serialPort.addDataListener(new SerialPortDataListener() {
        @Override
        public int getListeningEvents() {
          return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
          try {
            if (refresh) {
              refresh = false;
              s.onNext(ByteBuffer.allocate(0));
              binaryLogChannel.close();
            }
            buffer.clear();
            buffer.put(event.getReceivedData());
            buffer.flip();
            binaryLogChannel.write(buffer);
            buffer.rewind();
            logBytes(buffer);
            s.onNext(buffer);
          }
          catch (Exception ex) {
            try {
              LOGGER.log(LOG_LEVEL_ERRORS, ex, SerialService.this::toString);
              close();
            }
            finally {
              s.onComplete();
            }
          }
        }
      });
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
      synchronized (this) {
        if (isOpen() && serialPort != null) {
          LOGGER.log(Level.INFO, () -> "%s Close connection".formatted(String.valueOf(this)));
          serialPort.closePort();
        }
      }
    }
    finally {
      binaryLogChannel.close();
    }
  }

  @Override
  public void refresh() {
    LOGGER.log(Level.INFO, () -> "%s Refresh connection".formatted(String.valueOf(this)));
    refresh = true;
  }

  @Override
  public String toString() {
    if (serialPort == null) {
      return "%08x %s".formatted(hashCode(), SERIAL_PORT_NOT_FOUND);
    }
    else {
      return "%08x [%s] %s".formatted(hashCode(), serialPort.getSystemPortName(), serialPort.getDescriptivePortName());
    }
  }

  private enum Ports {
    INSTANCE;

    private final LinkedList<String> usedPorts = new LinkedList<>();

    @Nullable
    synchronized SerialPort next() {
      Collection<SerialPort> serialPorts = Arrays.stream(SerialPort.getCommPorts())
          .filter(port -> !port.getSystemPortName().toLowerCase().contains("bluetooth"))
          .sorted(Comparator.<SerialPort, Integer>comparing(port -> port.getSystemPortName().toLowerCase().indexOf("usb")).reversed())
          .sorted(Comparator.comparingInt(value -> usedPorts.indexOf(value.getSystemPortName()))).collect(Collectors.toUnmodifiableList());
      if (serialPorts.isEmpty()) {
        return null;
      }
      else {
        SerialPort serialPort = serialPorts.iterator().next();
        String portName = serialPort.getSystemPortName();
        LOGGER.log(LOG_LEVEL_ERRORS, () -> "Found { %s }, the [ %s ] is selected".formatted(serialPorts, portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return serialPort;
      }
    }
  }
}
