package com.ak.comm.serial;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SerialServiceTest {
  @Test
  @Disabled("use serial port")
  void testCycle() {
    SerialPort comPort = SerialPort.getCommPorts()[3];
    Logger.getLogger(getClass().getName()).info("[%s] %s".formatted(comPort.getSystemPortName(), comPort.getDescriptivePortName()));
    comPort.setBaudRate(115200);
    comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    Semaphore semaphore = new Semaphore(1);
    if (comPort.openPort()) {
      comPort.addDataListener(new SerialPortDataListener() {
        @Override
        public int getListeningEvents() {
          return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
          if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
            byte[] newData = event.getReceivedData();
            assertNotNull(newData);
            Logger.getLogger(getClass().getName()).info("%s : %s%n%s".formatted(event.getSerialPort(), Arrays.toString(newData),
                new String(newData, StandardCharsets.UTF_8)));
            semaphore.release();
          }
        }
      });

      int MM_3 = 200 * 16;
      int MM_FACTOR = 20;
      while (!Thread.currentThread().isInterrupted()) {
        IntStream.of(MM_3 / MM_FACTOR, -MM_3 / MM_FACTOR).forEach(value -> {
          byte[] buffer = "STEP %+d%n".formatted(value).getBytes(StandardCharsets.UTF_8);
          comPort.writeBytes(buffer, buffer.length);
          try {
            semaphore.acquire();
          }
          catch (InterruptedException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, e, () ->
                "[%s] %s".formatted(comPort.getSystemPortName(), comPort.getDescriptivePortName()));
          }
        });
      }
    }
  }

  @Test
  @Disabled("use serial port")
  void testSingle() {
    SerialPort comPort = SerialPort.getCommPorts()[3];
    Logger.getLogger(getClass().getName()).info("[%s] %s".formatted(comPort.getSystemPortName(), comPort.getDescriptivePortName()));
    comPort.setBaudRate(115200);
    comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    Semaphore semaphore = new Semaphore(1);
    if (comPort.openPort()) {
      comPort.addDataListener(new SerialPortDataListener() {
        @Override
        public int getListeningEvents() {
          return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
          if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
            byte[] newData = event.getReceivedData();
            assertNotNull(newData);
            Logger.getLogger(getClass().getName()).info("%s : %s%n%s".formatted(event.getSerialPort(), Arrays.toString(newData),
                new String(newData, StandardCharsets.UTF_8)));
            semaphore.release();
          }
        }
      });
      semaphore.acquireUninterruptibly();
    }
  }
}