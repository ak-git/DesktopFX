package com.ak.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CleanTest {
  private static final Logger LOGGER = Logger.getLogger(Clean.class.getName());

  @BeforeEach
  void setUp() {
    LOGGER.setFilter(r -> {
      assertThat(r.getMessage()).isNotNull();
      if (Objects.equals(r.getLevel(), Level.WARNING)) {
        assertThat(r.getThrown()).isNotNull();
      }
      return false;
    });
  }

  @AfterEach
  void tearDown() {
    LOGGER.setFilter(null);
  }

  @Nested
  class Classic {
    @Test
    void testClean() {
      try {
        Path root = Files.createTempDirectory("%s.".formatted(CleanTest.class.getPackageName()));
        Path tempFile = Files.createTempFile(root, Strings.EMPTY, Extension.TXT.attachTo(Strings.EMPTY));

        Path subDir = Files.createTempDirectory(root, Strings.EMPTY);
        Path tempFileSub = Files.createTempFile(subDir, Strings.EMPTY, Extension.CSV.attachTo(Strings.EMPTY));

        assertThatNoException().isThrownBy(() -> Clean.clean(() -> Clean.clean(root)));
        Assertions.assertAll("All paths and files are deleted",
            () -> assertThat(root).doesNotExist(),
            () -> assertThat(tempFile).doesNotExist(),
            () -> assertThat(subDir).doesNotExist(),
            () -> assertThat(tempFileSub).doesNotExist()
        );
        assertThatNoException().isThrownBy(() -> Clean.clean(() -> Clean.clean(root)));
      }
      catch (IOException e) {
        fail(e);
      }
    }
  }

  @Nested
  class Mocking {
    @Test
    void testPathNonExisting() throws IOException {
      Path path = Files.createTempDirectory("%s.".formatted(CleanTest.class.getPackageName()));
      assertThatNoException().isThrownBy(() -> Clean.clean(() -> Clean.clean(path)));
      try (MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
        mockFiles.when(() -> Files.deleteIfExists(path)).thenThrow(IOException.class);
        assertThatNoException().isThrownBy(() -> Clean.clean(() -> Clean.clean(path)));
      }
    }

    @Test
    void testSubPathsNonExisting() throws IOException {
      Path path = Files.createTempDirectory("%s.".formatted(CleanTest.class.getPackageName()));
      assertThatNoException().isThrownBy(() -> Clean.clean(() -> Clean.clean(path)));
      try (MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
        mockFiles.when(() -> Files.exists(path, LinkOption.NOFOLLOW_LINKS)).thenReturn(true);
        assertThatNoException().isThrownBy(() -> Clean.clean(() -> Clean.clean(path)));
      }
    }
  }
}