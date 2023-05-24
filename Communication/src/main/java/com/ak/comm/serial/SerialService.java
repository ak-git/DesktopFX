package com.ak.comm.serial;

import com.ak.comm.core.AbstractService;
import com.ak.comm.core.ConcurrentAsyncFileChannel;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.logging.OutputBuilders;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

final class SerialService<T, R> extends AbstractService<ByteBuffer> implements WritableByteChannel, Flow.Subscription {
  private static final Logger LOGGER = Logger.getLogger(SerialService.class.getName());
  private static final String SERIAL_PORT_NOT_FOUND = "Serial port not found";

  static {
    LOGGER.setFilter(new Filter() {
      private boolean notFoundFlag;

      @Override
      public boolean isLoggable(LogRecord logRecord) {
        if (SERIAL_PORT_NOT_FOUND.equals(logRecord.getMessage())) {
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
  @Nonnull
  private final ConcurrentAsyncFileChannel binaryLogChannel;
  @Nonnull
  private final AtomicReference<Path> saveFilePath = new AtomicReference<>();
  @Nullable
  private Runnable refreshAction;

  SerialService(@Nonnull BytesInterceptor<T, R> bytesInterceptor) {
    this.bytesInterceptor = bytesInterceptor;
    buffer = ByteBuffer.allocate(bytesInterceptor.getBaudRate());
    binaryLogChannel = new ConcurrentAsyncFileChannel(
        () -> {
          Path path = LogBuilders.SERIAL_BYTES.build(bytesInterceptor.name()).getPath();
          saveFilePath.set(path);
          return AsynchronousFileChannel.open(path, CREATE, WRITE);
        }
    );
  }

  @Override
  public boolean isOpen() {
    return serialPort != null && serialPort.isOpen();
  }

  @Override
  public int write(@Nonnull ByteBuffer src) {
    synchronized (this) {
      var countBytes = 0;
      if (isOpen() && serialPort != null) {
        src.rewind();
        countBytes = serialPort.writeBytes(src.array(), src.limit());
      }
      return countBytes;
    }
  }

  @Override
  public void subscribe(@Nonnull Flow.Subscriber<? super ByteBuffer> s) {
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
            if (refreshAction != null) {
              s.onNext(ByteBuffer.allocate(0));
              binaryLogChannel.close();
              refreshAction.run();
              refreshAction = null;
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
            LOGGER.log(LOG_LEVEL_ERRORS, ex, SerialService.this::toString);
            close();
            s.onComplete();
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
          LOGGER.log(Level.INFO, () -> "%s Close connection".formatted(this));
          serialPort.closePort();
        }
      }
    }
    finally {
      binaryLogChannel.close();
    }
  }

  @Override
  public void refresh(boolean save) {
    LOGGER.log(Level.INFO, () -> "%s Refresh connection".formatted(this));
    refreshAction = () -> {
      if (save) {
        try {
          Path source = saveFilePath.get();
          Path userCopy = OutputBuilders.NONE_WITH_DATE.build(source.toFile().getName()).getPath();
          CompletableFuture.runAsync(() -> {
            try {
              Files.copy(source, userCopy, REPLACE_EXISTING);
              LOGGER.log(Level.INFO, () -> "%s saved at %s".formatted(this, userCopy));
            }
            catch (IOException e) {
              Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
            }
          });
        }
        catch (IOException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
        }
      }
    };
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
          .sorted(Comparator.comparingInt(value -> usedPorts.indexOf(value.getSystemPortName())))
          .toList();
      if (serialPorts.isEmpty()) {
        return null;
      }
      else {
        var serialPort = serialPorts.iterator().next();
        String portName = serialPort.getSystemPortName();
        LOGGER.log(LOG_LEVEL_ERRORS, () -> "Found { %s }, the [ %s ] is selected".formatted(serialPorts, portName));
        usedPorts.remove(portName);
        usedPorts.addLast(portName);
        return serialPort;
      }
    }
  }
}
