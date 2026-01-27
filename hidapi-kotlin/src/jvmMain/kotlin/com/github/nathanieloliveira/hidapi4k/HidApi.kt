package com.github.nathanieloliveira.hidapi4k

import oshi.PlatformEnum
import oshi.SystemInfo
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object HidApiRaw {

    val WCHAR_CHARSET by lazy {
        val plat = SystemInfo.getCurrentPlatform()
        val order = ByteOrder.nativeOrder()
        when (plat) {
            PlatformEnum.MACOS, PlatformEnum.LINUX -> {
                if (order == ByteOrder.LITTLE_ENDIAN) {
                    StandardCharsets.UTF_32LE
                } else {
                    StandardCharsets.UTF_32BE
                }
            }
            PlatformEnum.WINDOWS -> StandardCharsets.UTF_16LE
            else -> error("Unsupported platform: $plat")
        }
    }

    fun MemorySegment.readWCharString(): String {
        return reinterpret(Long.MAX_VALUE).getString(0L, WCHAR_CHARSET)
    }

    private val hidInit by lazy {
        val f = Loader.lib.findOrThrow("hid_init")
        Loader.linker.downcallHandle(f, FunctionDescriptor.of(ValueLayout.JAVA_INT))
    }

    private val hidExit by lazy {
        val f = Loader.lib.findOrThrow("hid_exit")
        Loader.linker.downcallHandle(f, FunctionDescriptor.of(ValueLayout.JAVA_INT))
    }

    private val hidError by lazy {
        val f = Loader.lib.findOrThrow("hid_error")
        val desc = FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }


    private val hidEnumerate by lazy {
        val f = Loader.lib.findOrThrow("hid_enumerate")
        val desc = FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_SHORT,
            ValueLayout.JAVA_SHORT,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidFreeEnumeration by lazy {
        val f = Loader.lib.findOrThrow("hid_free_enumeration")
        val desc = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidOpen by lazy {
        val f = Loader.lib.findOrThrow("hid_open")
        val desc = FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_SHORT,
            ValueLayout.JAVA_SHORT,
            ValueLayout.ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidClose by lazy {
        val f = Loader.lib.findOrThrow("hid_close")
        val desc = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    fun hidInit(): Int {
        return hidInit.invoke() as Int
    }

    fun hidExit(): Int {
        return hidExit.invoke() as Int
    }

    fun hidError(device: Long): String {
        return Arena.ofConfined().use { arena ->
            val ptr = arena.allocate(ValueLayout.ADDRESS)
            ptr.set(ValueLayout.ADDRESS, 0L, MemorySegment.ofAddress(device))
            val errorCStr = hidError.invoke(ptr) as MemorySegment
            errorCStr.readWCharString()
        }
    }

    fun hidEnumerate(vendorId: Short, productId: Short): HidDeviceInfo? {
        val list = hidEnumerate.invoke(vendorId, productId) as MemorySegment
        if (list.address() == 0L) {
            return null
        }
        return HidDeviceInfo(list)
    }

    fun hidFreeEnumeration(devs: HidDeviceInfo) {
        hidFreeEnumeration.invoke(devs.pointer)
        devs.pointer = null
    }

    fun hidOpen(vendorId: Short, productId: Short, serialNumber: String?): HidDevice? {
        Arena.ofConfined().use { arena ->
            val wStr = serialNumber?.let {
                val bytes = WCHAR_CHARSET.encode(it)
                val w = arena.allocate(bytes.limit().toLong())
                MemorySegment.copy(
                    bytes.array(),
                    bytes.arrayOffset(),
                    w,
                    ValueLayout.JAVA_BYTE,
                    0L,
                    w.byteSize().toInt()
                )
                w
            }

            val dev = hidOpen(vendorId, productId, wStr ?: MemorySegment.NULL) as MemorySegment
            if (dev.address() == 0L) {
                return null
            }
            return HidDevice(dev)
        }
    }

    fun hidClose(device: HidDevice) {
        hidClose(device.pointer)
    }
}