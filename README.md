# Install on Linux

## Install [sdkman.io](https://sdkman.io)

```bash
curl -s "https://get.sdkman.io" | bash
```

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

## Install [JDK](https://www.oracle.com/java/technologies/downloads/)

```bash
sdk list java
```

```bash
sdk install java 23.0.1-oracle
```

## Install [Gradle](https://gradle.org)

```bash
sdk list gradle
```

```bash
sdk install gradle 8.12.1
```

## Verify Installation

```bash
sdk current
```