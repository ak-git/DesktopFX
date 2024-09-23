package com.ak.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OSDirectoryTest {
  @Nested
  class Valid {
    private enum OSTest implements OSDirectory {
      WINDOWS, MAC, UNIX;

      @Override
      public Path getDirectory() {
        return OSDirectories.getDirectory(Strings.EMPTY);
      }
    }

    @Test
    @DisplayName("Enum contains WINDOWS, MAC, UNIX")
    void testOf() {
      assertThat(OSDirectory.of(OSTest.class).getDirectory()).isDirectory().exists();
    }
  }

  @Nested
  class Invalid {
    private enum OSTest implements OSDirectory {
      ONE, TWO;

      @Override
      public Path getDirectory() {
        return OSDirectories.getDirectory(Strings.EMPTY);
      }
    }

    @Test
    @DisplayName("Enum contains invalid OS")
    void testOf() {
      assertThatIllegalArgumentException().isThrownBy(() -> OSDirectory.of(OSTest.class).getDirectory());
    }
  }

  @Nested
  class Mocking {
    private static final Logger LOGGER = Logger.getLogger(OSDirectories.class.getName());
    @Mock
    private Path path;

    @BeforeEach
    void setUp() {
      LOGGER.setFilter(r -> {
        assertThat(r.getMessage()).isNotNull();
        assertAll(r.getMessage(),
            () -> assertThat(r.getLevel()).isEqualTo(Level.WARNING),
            () -> assertThat(r.getThrown()).isNotNull()
        );
        return false;
      });
    }

    @AfterEach
    void tearDown() {
      LOGGER.setFilter(null);
    }

    @Test
    void testPathNonExisting() {
      assertThatNoException().isThrownBy(() -> OSDirectories.getDirectory(Strings.EMPTY));
      try (MockedStatic<Files> mockFiles = mockStatic(Files.class);
           MockedStatic<Paths> mockPaths = mockStatic(Paths.class)) {
        mockFiles.when(() -> Files.createDirectories(any())).thenThrow(IOException.class);
        mockFiles.when(() -> Files.isDirectory(any())).thenReturn(true);
        mockFiles.when(() -> Files.isWritable(any())).thenReturn(true);
        mockFiles.when(() -> Files.exists(any())).thenReturn(true);
        mockPaths.when(() -> Paths.get(anyString())).thenReturn(path);
        when(path.resolve(anyString())).thenReturn(path);
        assertThatNoException().isThrownBy(() -> OSDirectories.getDirectory(Strings.EMPTY));
      }
    }
  }
}