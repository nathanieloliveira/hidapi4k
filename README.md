# hidapi4k

---

hidapi4k is a JVM 25 FFM binding library for [libusb/hidapi](https://github.com/libusb/hidapi) written in Kotlin.

## Supported Platforms

| JVM Platform   | Status |
|----------------|:------:|
| Windows x86_64 |  🛠️   |
| Windows arm64  |  🛠️   |
| Linux x86_64   |   ✅    |
| Linux arm64    |   ⚠️   |
| macOS x86_64   |  🛠️   |
| macOS arm64    |  🛠️   |

- 🛠 Work-in-progress
- ✅ Tested and working
- ⚠️ Works in theory. Not tested

## Minimum Requirements

* JVM 25
* Kotlin 2.3.0
* libudev<sup>1</sup>

<sup>1</sup> Linux only. hidapi itself needs it, not this binding library.

## hidapi-ffm

This module exposes the raw API declared in `hidapi.h` with "some" syntax sugar, particularly 
when dealing with `wchar_t*` strings.
