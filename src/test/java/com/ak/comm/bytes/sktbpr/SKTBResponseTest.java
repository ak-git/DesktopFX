package com.ak.comm.bytes.sktbpr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SKTBResponseTest {
  @Test
  void testRequestOk() {
    SKTBResponse.Builder builder = new SKTBResponse.Builder();
    builder.buffer().put((byte) 0xa5);
    assertThat(builder.is((byte) 0xa5)).isTrue();
    builder.buffer().put((byte) 0x00);

    builder.buffer().put((byte) 6);
    assertThat(builder.is((byte) 6)).isTrue();

    builder.buffer().rewind();
    assertThat(builder.build()).isNotNull();
  }

  @Test
  void testRequestFall() {
    SKTBResponse.Builder builder = new SKTBResponse.Builder();
    builder.buffer().put((byte) 0x5a);
    assertThat(builder.is((byte) 0x5a)).isFalse();
    builder.buffer().put((byte) 0x00);

    builder.buffer().put((byte) 7);
    assertThat(builder.is((byte) 7)).isFalse();

    builder.buffer().rewind();
    assertThat(builder.build()).isNull();
  }
}