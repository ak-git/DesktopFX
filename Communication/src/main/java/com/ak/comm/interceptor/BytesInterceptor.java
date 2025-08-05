package com.ak.comm.interceptor;

import com.ak.util.Strings;
import com.fazecast.jSerialComm.SerialPort;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public interface BytesInterceptor<T, R> extends Function<ByteBuffer, Stream<R>> {
  enum SerialParams implements Consumer<SerialPort> {
    CLEAR_DTR {
      @Override
      public void accept(SerialPort serialPort) {
        serialPort.clearDTR();
      }
    },
    ODD_PARITY {
      @Override
      public void accept(SerialPort serialPort) {
        serialPort.setParity(SerialPort.ODD_PARITY);
      }
    },
    DATA_BITS_7 {
      @Override
      public void accept(SerialPort serialPort) {
        serialPort.setNumDataBits(7);
      }
    }
  }

  enum BaudRate implements IntSupplier {
    BR_9600, BR_38400, BR_57600, BR_115200, BR_460800, BR_921600;

    @Override
    public int getAsInt() {
      return Integer.parseInt(name().replaceFirst("^\\D*", Strings.EMPTY));
    }
  }

  String name();

  int getBaudRate();

  default Set<SerialParams> getSerialParams() {
    return Collections.emptySet();
  }

  /**
   * Process input bytes buffer.<br/>
   *
   * @param src input bytes buffer
   * @return response's stream
   */
  @Override
  Stream<R> apply(ByteBuffer src);

  Optional<T> getPingRequest();

  /**
   * Converts object to bytes and puts them into output buffer.
   *
   * @param request an object to convert and send out
   * @return output bytes buffer with object converted
   */
  ByteBuffer putOut(T request);
}
