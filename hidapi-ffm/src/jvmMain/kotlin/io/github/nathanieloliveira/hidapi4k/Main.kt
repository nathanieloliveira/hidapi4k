package io.github.nathanieloliveira.hidapi4k

import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout

fun main() {
    Loader.load()
    println("Hello Kotlin!")

    val version = HidApi.hidVersion()
    println(version)
    val versionStr = HidApi.hidVersionStr()
    println(versionStr)

    HidApi.hidInit()
    println("inited!")

//    val list = HidApiRaw.hidEnumerate(0x1212, 0x1414)
    val list = HidApi.hidEnumerate(0, 0)
    if (list == null) {
        val error = HidApi.hidError(null)
        println(error)
        return
    }

    var dev = list

    while (dev != null) {
        println(dev)
        dev = dev.next
    }

    HidApi.hidFreeEnumeration(list)

    // vendorId=0x1b1c, productId=0x0c0b, serialNumber='1E200406899045AF4D5E3B5CC31B00F5'
    val open = HidApi.hidOpen(0x1b1c, 0x0c0b, "1E200406899045AF4D5E3B5CC31B00F5")
    if (open != null) {
        val success = HidApi.hidSetNonblocking(open, true)
        println("setNonblocking: $success")
        HidApi.hidClose(open)
    }

    val dev2 = HidApi.hidOpenPath("/dev/hidraw9")
    if (dev2 == null) {
        val error = HidApi.hidError(null)
        println(error)
        return
    }

    Arena.ofConfined().use { arena ->
        val buffer = arena.allocate(1024)
        var result = HidApi.hidWrite(dev2, buffer, 64)
        if (result < 0) {
            val error = HidApi.hidError(dev2)
            println(error)
        }

        result = HidApi.hidRead(dev2, buffer, 0)
        if (result < 0) {
            val error = HidApi.hidReadError(dev2)
            println(error)
        }

        val reportDescriptorLen = HidApi.hidGetReportDescriptor(dev2, buffer, 1024)
        if (reportDescriptorLen < 0) {
            val error = HidApi.hidError(dev2)
            println(error)
            return@use
        }

        val readBytes = buffer.reinterpret(reportDescriptorLen.toLong()).toArray(ValueLayout.JAVA_BYTE)
        println(readBytes.toHexString())

        val infoFromDev = HidApi.hidGetDeviceInfo(dev2)
        println(infoFromDev)


    }

    HidApi.hidExit()
}