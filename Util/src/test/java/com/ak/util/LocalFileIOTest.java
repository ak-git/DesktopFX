package com.ak.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class LocalFileIOTest {
  private static final Path PATH;

  static {
    try {
      PATH = Files.createTempDirectory("%s.".formatted(LocalFileIOTest.class.getPackageName()));
    }
    catch (IOException e) {
      fail(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @AfterAll
  static void cleanUp() {
    Clean.clean(Objects.requireNonNull(PATH));
  }

  private enum TestOSDirectory implements OSDirectory {
    WINDOWS, MAC, UNIX;

    @Override
    public Path getDirectory() {
      return PATH;
    }
  }

  @ParameterizedTest
  @EnumSource(Extension.class)
  void testRoot(Extension extension) throws IOException {
    LocalIO localIO = new LocalFileIO.AbstractBuilder(extension) {
      @Override
      public LocalIO build() {
        return new LocalFileIO(this, OSDirectory.of(TestOSDirectory.class));
      }
    }.build();
    assertThat(localIO.getPath()).isEqualTo(PATH).exists();
  }

  @ParameterizedTest
  @EnumSource(Extension.class)
  void testChildThatNotExist(Extension extension) throws IOException {
    LocalIO localIO = new LocalFileIO.AbstractBuilder(extension) {
      @Override
      public LocalIO build() {
        return new LocalFileIO(this, OSDirectory.of(TestOSDirectory.class));
      }
    }.addPathWithDate().addPathWithDate().build();
    String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ZonedDateTime.now());
    assertThat(localIO.getPath()).isEqualTo(PATH.resolve(date).resolve(date)).exists();
  }

  @ParameterizedTest
  @EnumSource(Extension.class)
  void testFileThatNotExist(Extension extension) throws IOException {
    LocalIO localIO = new LocalFileIO.AbstractBuilder(extension) {
      @Override
      public LocalIO build() {
        return new LocalFileIO(this, OSDirectory.of(TestOSDirectory.class));
      }
    }.fileNameWithDateTime(LocalFileIO.class.getSimpleName()).build();
    assertThat(localIO.getPath().toString())
        .startsWith(PATH.toString())
        .contains(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-").format(ZonedDateTime.now()))
        .endsWith(extension.attachTo(LocalFileIO.class.getSimpleName()));
    assertThat(localIO.getPath()).doesNotExist();
    Assertions.assertThrows(NoSuchFileException.class, localIO::openInputStream);
  }

  @ParameterizedTest
  @EnumSource(Extension.class)
  void testFileThatExist(Extension extension) throws IOException {
    LocalIO localIO = new LocalFileIO.AbstractBuilder(extension) {
      @Override
      public LocalIO build() {
        return new LocalFileIO(this, OSDirectory.of(TestOSDirectory.class));
      }
    }.addPathWithDate().fileNameWithDateTime(LocalFileIO.class.getSimpleName()).build();
    Files.createFile(localIO.getPath());
    assertThat(localIO.getPath().toString())
        .startsWith(PATH.toString())
        .contains(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-").format(ZonedDateTime.now()))
        .endsWith(extension.attachTo(LocalFileIO.class.getSimpleName()));
    assertThat(localIO.getPath()).exists();
    assertThat(localIO.openInputStream()).isEmpty();
  }
}