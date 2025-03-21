package com.ak.comm.serial;

import com.ak.comm.core.AbstractService;
import com.ak.comm.core.ConcurrentAsyncFileChannel;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.logging.LogBuilders;
import com.ak.logging.OutputBuilders;
import com.ak.util.Strings;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_BYTES;
import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

final class SerialService<T, R> extends AbstractService<ByteBuffer> implements WritableByteChannel, Flow.Subscription {
  private static final Logger LOGGER = Logger.getLogger(SerialService.class.getName());
  private static final String SERIAL_PORT_NOT_FOUND = "Serial port not found";
  private static final AtomicInteger PORT_INDEX = new AtomicInteger();

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

  private final @Nullable SerialPort serialPort;
  private final BytesInterceptor<T, R> bytesInterceptor;
  private final ByteBuffer buffer;
  private final ConcurrentAsyncFileChannel binaryLogChannel;
  private final AtomicReference<Path> saveFilePath = new AtomicReference<>();
  private @Nullable Runnable refreshAction;

  SerialService(BytesInterceptor<T, R> bytesInterceptor) {
    this.bytesInterceptor = bytesInterceptor;
    serialPort = next().orElse(null);
    buffer = ByteBuffer.allocate(bytesInterceptor.getBaudRate());
    binaryLogChannel = new ConcurrentAsyncFileChannel(
        () -> {
          Path path = LogBuilders.SERIAL_BYTES.build(bytesInterceptor.name()).getPath();
          saveFilePath.set(path);
          return Optional.of(AsynchronousFileChannel.open(path, CREATE, WRITE));
        }
    );
  }

  @Override
  public boolean isOpen() {
    return serialPort != null && serialPort.isOpen();
  }

  @Override
  public int write(ByteBuffer src) {
    synchronized (this) {
      var countBytes = 0;
      if (isOpen() && serialPort != null) {
        src.rewind();
        LOGGER.log(LOG_LEVEL_BYTES, () -> "%s %s".formatted(this, new String(src.array(), 0, src.limit())));
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
      bytesInterceptor.getSerialParams().forEach(serialParams -> serialParams.accept(serialPort));
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
    StringJoiner joiner = new StringJoiner(Strings.SPACE);
    joiner.add("#%08x".formatted(hashCode()));
    if (serialPort == null) {
      joiner.add(SERIAL_PORT_NOT_FOUND);
    }
    else {
      joiner.add(serialPort.getSystemPortName()).add("'%s'".formatted(serialPort.getDescriptivePortName()));
    }
    return joiner.toString();
  }

  private static Optional<SerialPort> next() {
    List<SerialPort> serialPorts = Arrays.stream(SerialPort.getCommPorts())
        .filter(port -> !port.getSystemPortName().toLowerCase().contains("bluetooth"))
        .sorted(Comparator.<SerialPort, Integer>comparing(port -> port.getSystemPortName().toLowerCase().indexOf("usb")).reversed())
        .toList();
    if (serialPorts.isEmpty()) {
      return Optional.empty();
    }
    else {
      var selectedPort = serialPorts.get(PORT_INDEX.getAndIncrement() % serialPorts.size());
      LOGGER.log(LOG_LEVEL_ERRORS, () -> "Found %s, the %s is selected"
          .formatted(serialPorts.stream().map(SerialPort::getSystemPortName).toList(), selectedPort.getSystemPortName())
      );
      return Optional.of(selectedPort);
    }
  }
}
