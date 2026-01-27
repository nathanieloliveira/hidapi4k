package com.github.nathanieloliveira.hidapi4k

import com.github.nathanieloliveira.hidapi4k.HidApiRaw.readWCharString
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.charset.StandardCharsets

enum class HidBusType(val value: Int) {
    Unknown(0x00),
    USB(0x01),
    Bluetooth(0x02),
    I2C(0x03),
    SPI(0x04),
    Virtual(0x05),;

    companion object {
        fun fromInt(value: Int): HidBusType {
            return entries.firstOrNull { it.value == value } ?: Unknown
        }
    }
}

class HidDeviceInfo(
    pointer: MemorySegment
) {
    companion object {
        val LAYOUT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("path"),
            ValueLayout.JAVA_SHORT.withName("vendor_id"),
            ValueLayout.JAVA_SHORT.withName("product_id"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withName("serial_number"),
            MemoryLayout.paddingLayout(6),
            ValueLayout.JAVA_SHORT.withName("release_number"),
            ValueLayout.ADDRESS.withName("manufacturer_string"),
            ValueLayout.ADDRESS.withName("product_string"),
            ValueLayout.JAVA_SHORT.withName("usage_page"),
            ValueLayout.JAVA_SHORT.withName("usage"),
            ValueLayout.JAVA_INT.withName("interface_number"),
            ValueLayout.ADDRESS.withName("next"),
            ValueLayout.JAVA_INT.withName("bus_type"),
        ).withName("hid_device_info")

        val PATH_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("path"))
        val VENDOR_ID_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("vendor_id"))
        val PRODUCT_ID_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("product_id"))
        val SERIAL_NUMBER_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("serial_number"))
        val RELEASE_NUMBER_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("release_number"))
        val MANUFACTURER_STRING_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("manufacturer_string"))
        val PRODUCT_STRING_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("product_string"))
        val USAGE_PAGE_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("usage_page"))
        val USAGE_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("usage"))
        val INTERFACE_NUMBER_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("interface_number"))
        val NEXT_HANDLE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("next"))
        val BUS_TYPE = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("bus_type"))
    }

    var pointer: MemorySegment? = pointer.reinterpret(LAYOUT.byteSize())

    val path: String
        get() {
            val cStr = PATH_HANDLE.get(pointer, 0L) as MemorySegment
            return cStr.reinterpret(Long.MAX_VALUE).getString(0L, StandardCharsets.UTF_8)
        }

    val vendorId: Short
        get() = VENDOR_ID_HANDLE.get(pointer, 0L) as Short

    val productId: Short
        get() = PRODUCT_ID_HANDLE.get(pointer, 0L) as Short

    val serialNumber: String
        get() {
            val cStr = SERIAL_NUMBER_HANDLE.get(pointer, 0L) as MemorySegment
            return cStr.readWCharString()
        }

    val releaseNumber: Short
        get() = RELEASE_NUMBER_HANDLE.get(pointer, 0L) as Short

    val manufacturerString: String
        get() {
            val cStr = MANUFACTURER_STRING_HANDLE.get(pointer, 0L) as MemorySegment
            return cStr.readWCharString()
        }

    val productString: String
        get() {
            val cStr = PRODUCT_STRING_HANDLE.get(pointer, 0L) as MemorySegment
            return cStr.readWCharString()
        }

    val usagePage: Short
        get() = USAGE_PAGE_HANDLE.get(pointer, 0L) as Short

    val usage: Short
        get() = USAGE_HANDLE.get(pointer, 0L) as Short

    val interfaceNumber: Int
        get() = INTERFACE_NUMBER_HANDLE.get(pointer, 0L) as Int

    val next: HidDeviceInfo?
        get() {
            val nextPtr = NEXT_HANDLE.get(pointer, 0L) as MemorySegment
            if (nextPtr.address() == 0L) {
                return null
            }
            return HidDeviceInfo(nextPtr)
        }

    val busType: HidBusType
        get() {
            val type = BUS_TYPE.get(pointer, 0L) as Int
            return HidBusType.fromInt(type)
        }

    override fun toString(): String {
        if (pointer == null) {
            return "null"
        }
        return "HidDeviceInfo(path='$path', vendorId=0x${vendorId.toHexString().padStart(4, '0')}, productId=0x${productId.toHexString().padStart(4, '0')}, serialNumber='$serialNumber', releaseNumber=$releaseNumber, manufacturerString='$manufacturerString', productString='$productString', usagePage=$usagePage, usage=$usage, interfaceNumber=$interfaceNumber, next=${next?.pointer?.address()?.toHexString()}, busType=$busType)"
    }

}

@JvmInline
value class HidDevice(val pointer: MemorySegment)