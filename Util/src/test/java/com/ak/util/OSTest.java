package com.ak.util;

import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OSTest {
  private static final Logger LOGGER = Logger.getLogger(OS.MAC.getClass().getName());

  @BeforeEach
  public void setUp() {
    LOGGER.setFilter(record -> {
      assertNotNull(record.getThrown());
      return false;
    });
  }

  @Test
  void testGet() {
    assertThat(Stream.of(OS.values()).filter(BooleanSupplier::getAsBoolean)).hasSize(1);
  }

  @AfterEach
  void tearDown() {
    LOGGER.setFilter(null);
  }
}