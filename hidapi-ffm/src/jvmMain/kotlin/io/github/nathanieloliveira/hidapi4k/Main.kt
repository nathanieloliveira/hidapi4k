package io.github.nathanieloliveira.hidapi4k

import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout

fun main() {
    Loader.load()
    println("Hello Kotlin!")

    val version = HidApiRaw.hidVersion()
    println(version)
    val versionStr = HidApiRaw.hidVersionStr()
    println(versionStr)

    HidApiRaw.hidInit()
    println("inited!")

//    val list = HidApiRaw.hidEnumerate(0x1212, 0x1414)
    val list = HidApiRaw.hidEnumerate(0, 0)
    if (list == null) {
        val error = HidApiRaw.hidError(null)
        println(error)
        return
    }

    var dev = list

    while (dev != null) {
        println(dev)
        dev = dev.next
    }

    HidApiRaw.hidFreeEnumeration(list)

    // vendorId=0x1b1c, productId=0x0c0b, serialNumber='1E200406899045AF4D5E3B5CC31B00F5'
    val open = HidApiRaw.hidOpen(0x1b1c, 0x0c0b, "1E200406899045AF4D5E3B5CC31B00F5")
    if (open != null) {
        val success = HidApiRaw.hidSetNonblocking(open, true)
        println("setNonblocking: $success")
        HidApiRaw.hidClose(open)
    }

    val dev2 = HidApiRaw.hidOpenPath("/dev/hidraw9")
    if (dev2 == null) {
        val error = HidApiRaw.hidError(null)
        println(error)
        return
    }

    Arena.ofConfined().use { arena ->
        val buffer = arena.allocate(1024)
        var result = HidApiRaw.hidWrite(dev2, buffer, 64)
        if (result < 0) {
            val error = HidApiRaw.hidError(dev2)
            println(error)
        }

        result = HidApiRaw.hidRead(dev2, buffer, 0)
        if (result < 0) {
            val error = HidApiRaw.hidReadError(dev2)
            println(error)
        }

        val reportDescriptorLen = HidApiRaw.hidGetReportDescriptor(dev2, buffer, 1024)
        if (reportDescriptorLen < 0) {
            val error = HidApiRaw.hidError(dev2)
            println(error)
            return@use
        }

        val readBytes = buffer.reinterpret(reportDescriptorLen.toLong()).toArray(ValueLayout.JAVA_BYTE)
        println(readBytes.toHexString())

        val infoFromDev = HidApiRaw.hidGetDeviceInfo(dev2)
        println(infoFromDev)


    }

    HidApiRaw.hidExit()
}