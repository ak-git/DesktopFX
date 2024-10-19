package com.ak.comm.interceptor;

import com.fazecast.jSerialComm.SerialPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BytesInterceptorTest {
  @Mock
  private SerialPort serialPort;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(serialPort);
  }

  @Test
  void testClearDTR() {
    BytesInterceptor.SerialParams.CLEAR_DTR.accept(serialPort);
    Mockito.verify(serialPort).clearDTR();
  }

  @Test
  void testOddParity() {
    BytesInterceptor.SerialParams.ODD_PARITY.accept(serialPort);
    ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(serialPort).setParity(captor.capture());
    Assertions.assertThat(captor.getValue()).isEqualTo(SerialPort.ODD_PARITY);
  }

  @Test
  void testDataBits7() {
    BytesInterceptor.SerialParams.DATA_BITS_7.accept(serialPort);
    ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(serialPort).setNumDataBits(captor.capture());
    Assertions.assertThat(captor.getValue()).isEqualTo(7);
  }
}