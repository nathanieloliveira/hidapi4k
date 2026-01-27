package io.github.nathanieloliveira.hidapi4k

import oshi.PlatformEnum
import oshi.SystemInfo
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object HidApi {

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
        Loader.linker.downcallHandle(f, FunctionDescriptor.of(JAVA_INT))
    }

    private val hidExit by lazy {
        val f = Loader.lib.findOrThrow("hid_exit")
        Loader.linker.downcallHandle(f, FunctionDescriptor.of(JAVA_INT))
    }

    private val hidEnumerate by lazy {
        val f = Loader.lib.findOrThrow("hid_enumerate")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            ValueLayout.JAVA_SHORT,
            ValueLayout.JAVA_SHORT,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidFreeEnumeration by lazy {
        val f = Loader.lib.findOrThrow("hid_free_enumeration")
        val desc = FunctionDescriptor.ofVoid(ADDRESS)
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidOpen by lazy {
        val f = Loader.lib.findOrThrow("hid_open")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            ValueLayout.JAVA_SHORT,
            ValueLayout.JAVA_SHORT,
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidOpenPath by lazy {
        val f = Loader.lib.findOrThrow("hid_open_path")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidWrite by lazy {
        val f = Loader.lib.findOrThrow("hid_write")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidReadTimeout by lazy {
        val f = Loader.lib.findOrThrow("hid_read_timeout")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
            JAVA_INT,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidRead by lazy {
        val f = Loader.lib.findOrThrow("hid_read_timeout")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidReadError by lazy {
        val f = Loader.lib.findOrThrow("hid_read_error")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidSetNonblocking by lazy {
        val f = Loader.lib.findOrThrow("hid_set_nonblocking")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            JAVA_INT,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidSendFeatureReport by lazy {
        val f = Loader.lib.findOrThrow("hid_send_feature_report")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetFeatureReport by lazy {
        val f = Loader.lib.findOrThrow("hid_get_feature_report")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidSendOutputReport by lazy {
        val f = Loader.lib.findOrThrow("hid_send_output_report")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetInputReport by lazy {
        val f = Loader.lib.findOrThrow("hid_get_input_report")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidClose by lazy {
        val f = Loader.lib.findOrThrow("hid_close")
        val desc = FunctionDescriptor.ofVoid(
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetManufacturerString by lazy {
        val f = Loader.lib.findOrThrow("hid_get_manufacturer_string")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetProductString by lazy {
        val f = Loader.lib.findOrThrow("hid_get_product_string")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetSerialNumberString by lazy {
        val f = Loader.lib.findOrThrow("hid_get_serial_number_string")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetDeviceInfo by lazy {
        val f = Loader.lib.findOrThrow("hid_get_device_info")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetIndexedString by lazy {
        val f = Loader.lib.findOrThrow("hid_get_indexed_string")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            JAVA_INT,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidGetReportDescriptor by lazy {
        val f = Loader.lib.findOrThrow("hid_get_report_descriptor")
        val desc = FunctionDescriptor.of(
            JAVA_INT,
            ADDRESS,
            ADDRESS,
            JAVA_LONG,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidError by lazy {
        val f = Loader.lib.findOrThrow("hid_error")
        val desc = FunctionDescriptor.of(
            ADDRESS,
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidVersion by lazy {
        val f = Loader.lib.findOrThrow("hid_version")
        val desc = FunctionDescriptor.of(
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    private val hidVersionStr by lazy {
        val f = Loader.lib.findOrThrow("hid_version_str")
        val desc = FunctionDescriptor.of(
            ADDRESS,
        )
        Loader.linker.downcallHandle(f, desc)
    }

    fun hidInit(): Int {
        return hidInit.invoke() as Int
    }

    fun hidExit(): Int {
        return hidExit.invoke() as Int
    }

    fun hidEnumerate(vendorId: Short, productId: Short): HidDeviceInfo? {
        val list = hidEnumerate.invoke(vendorId, productId) as MemorySegment
        if (list.address() == 0L) {
            return null
        }
        return HidDeviceInfo(list)
    }

    fun hidFreeEnumeration(devs: HidDeviceInfo) {
        val ptr = devs.pointer
        devs.pointer = null
        // safety: cannot go through devs.pointer after calling free. will cause segmentation fault
        //          so preemptively set this proxy objects pointer to null
        hidFreeEnumeration.invoke(ptr)
    }

    fun hidOpen(vendorId: Short, productId: Short, serialNumber: String?): HidDevice? {
        Arena.ofConfined().use { arena ->
            val wStr = serialNumber?.let {
                arena.allocateFrom(serialNumber, WCHAR_CHARSET)
            } ?: MemorySegment.NULL

            val dev = hidOpen(vendorId, productId, wStr) as MemorySegment
            if (dev.address() == 0L) {
                return null
            }
            return HidDevice(dev)
        }
    }

    fun hidOpenPath(path: String): HidDevice? {
        Arena.ofConfined().use { arena ->
            val pathCStr = arena.allocateFrom(path, StandardCharsets.UTF_8)
            val dev = hidOpenPath.invoke(pathCStr) as MemorySegment
            if (dev.address() == 0L) {
                return null
            }
            return HidDevice(dev)
        }
    }

    fun hidWrite(device: HidDevice, data: MemorySegment, length: Long): Int {
        return hidWrite.invoke(device.pointer, data, length) as Int
    }

    fun hidReadTimeout(device: HidDevice, data: MemorySegment, length: Long, milliseconds: Int): Int {
        return hidReadTimeout.invoke(device.pointer, data, length, milliseconds) as Int
    }

    fun hidRead(device: HidDevice, data: MemorySegment, length: Long): Int {
        return hidRead.invoke(device.pointer, data, length) as Int
    }

    fun hidReadError(device: HidDevice): String {
        val errorCStr = hidReadError.invoke(device.pointer) as MemorySegment
        return errorCStr.readWCharString()
    }

    fun hidSetNonblocking(dev: HidDevice, nonblock: Boolean): Int {
        return hidSetNonblocking.invoke(dev.pointer, if (nonblock) 1 else 0) as Int
    }

    fun hidSendFeatureReport(dev: HidDevice, data: MemorySegment, length: Long): Int {
        return hidSendFeatureReport.invoke(dev.pointer, data, length) as Int
    }

    fun hidGetFeatureReport(dev: HidDevice, data: MemorySegment, length: Long): Int {
        return hidGetFeatureReport.invoke(dev.pointer, data, length) as Int
    }

    fun hidSendOutputReport(dev: HidDevice, data: MemorySegment, length: Long): Int {
        return hidSendOutputReport.invoke(dev.pointer, data, length) as Int
    }

    fun hidGetInputReport(dev: HidDevice, data: MemorySegment, length: Long): Int {
        return hidGetInputReport.invoke(dev.pointer, data, length) as Int
    }

    fun hidClose(device: HidDevice) {
        hidClose(device.pointer)
    }

    fun hidGetManufacturerString(dev: HidDevice, string: MemorySegment, maxlen: Long): Int {
        return hidGetManufacturerString.invoke(dev.pointer, string, maxlen) as Int
    }

    fun hidGetProductString(dev: HidDevice, string: MemorySegment, maxlen: Long): Int {
        return hidGetProductString.invoke(dev.pointer, string, maxlen) as Int
    }

    fun hidGetSerialNumberString(dev: HidDevice, string: MemorySegment, maxlen: Long): Int {
        return hidGetSerialNumberString.invoke(dev.pointer, string, maxlen) as Int
    }

    fun hidGetDeviceInfo(dev: HidDevice): HidDeviceInfo {
        val ptr = hidGetDeviceInfo.invoke(dev.pointer) as MemorySegment
        return HidDeviceInfo(ptr)
    }

    fun hidGetIndexedString(dev: HidDevice, stringIndex: Int, string: MemorySegment, maxlen: Long): Int {
        return hidGetIndexedString.invoke(dev.pointer, stringIndex, string, maxlen) as Int
    }

    fun hidGetReportDescriptor(dev: HidDevice, string: MemorySegment, bufSize: Long): Int {
        return hidGetReportDescriptor.invoke(dev.pointer, string, bufSize) as Int
    }

    fun hidError(device: HidDevice?): String {
        val errorCStr = hidError.invoke(device?.pointer ?: MemorySegment.NULL) as MemorySegment
        return errorCStr.readWCharString()
    }

    fun hidVersion(): HidApiVersion {
        val pointer = hidVersion.invoke() as MemorySegment
        return HidApiVersion(pointer)
    }

    fun hidVersionStr(): String {
        val cStr = hidVersionStr.invoke() as MemorySegment
        return cStr.reinterpret(Long.MAX_VALUE).getString(0L, StandardCharsets.UTF_8)
    }

}