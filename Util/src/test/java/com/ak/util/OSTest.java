package com.ak.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OSTest {
  private static final Logger LOGGER = Logger.getLogger(OS.MAC.getClass().getName());

  @BeforeAll
  static void setUp() {
    LOGGER.setFilter(r -> {
      assertNotNull(r.getThrown());
      return false;
    });
  }

  @Test
  void testGet() {
    assertThat(Stream.of(OS.values()).filter(BooleanSupplier::getAsBoolean)).hasSize(1);
  }

  @AfterAll
  static void tearDown() {
    LOGGER.setFilter(null);
  }
}