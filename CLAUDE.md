# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

hidapi4k is a JVM 25 FFM (Foreign Function & Memory) binding library for [libusb/hidapi](https://github.com/libusb/hidapi), written in Kotlin. It uses Java's `java.lang.foreign` API to call native hidapi functions at runtime, without JNI.

## Build Commands

```bash
# Build all modules
./gradlew build

# Build native library (requires Zig 0.15.2 and libudev-dev on Linux)
cd zig && zig build -Doptimize=ReleaseFast

# Download pre-built native libraries and assemble JARs
./gradlew :hidapi-natives:assemble

# Run the sample CLI app
./gradlew :sample-cli:jvmRun

# Run tests for hidapi-ffm
./gradlew :hidapi-ffm:jvmTest
```

## Architecture

### Modules

- **hidapi-ffm** — Core binding module. Contains the FFM API surface:
  - `HidApi.kt` — Singleton exposing all `hid_*` functions via `java.lang.foreign.Linker` downcall handles (lazy-initialized). Each function finds its symbol in the loaded native library and creates a `MethodHandle`.
  - `Loader.kt` — Loads the native hidapi shared library. Checks `HIDAPI4K_LIBRARY_PATH` system property first, otherwise unpacks a bundled native from classpath resources to `~/.hidapi4k/`.
  - `Structs.kt` — FFM `MemoryLayout` definitions for C structs (`hid_device_info`, `hid_api_version`). Uses `VarHandle` for field access. `HidDevice` is an inline value class wrapping a `MemorySegment` pointer.

- **hidapi-natives** — Downloads pre-built native binaries from GitHub Releases and packages them as classifier JARs (e.g., `linux-x86_64`, `windows-aarch64`). Resources are placed at `hidapi4k/{os}/{arch}/` inside the JAR.

- **hidapi** — Git submodule containing the upstream libusb/hidapi C source.

- **zig** — Zig build system that compiles the hidapi C source into shared libraries. Used by CI to produce native binaries.

- **sample-cli** — CLI sample using Clikt that demonstrates device enumeration (`ls` command).

- **composeApp** — Compose Multiplatform desktop GUI app (scaffolding, not yet integrated with hidapi).

### Key Technical Details

- Requires **JVM 25** (Adoptium toolchain) — uses preview FFM APIs.
- Kotlin **2.3.0** with Kotlin Multiplatform plugin (JVM target only currently).
- Platform detection uses JNA's `com.sun.jna.Platform` for OS/arch checks.
- `wchar_t*` strings use UTF-32 on Linux/macOS and UTF-16LE on Windows.
- Native libraries are resolved at `hidapi4k/{os}/{arch}/{libname}` in classpath resources.

### CI

GitHub Actions workflow (`.github/workflows/build.yml`) builds native libraries on tag push (`natives-X.Y.Z`). Builds for Windows (x64/arm64), Linux (x64/arm64), and macOS (arm64, x86_64 cross-compiled).
