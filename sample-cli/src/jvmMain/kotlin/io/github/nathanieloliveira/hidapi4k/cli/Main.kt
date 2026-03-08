package io.github.nathanieloliveira.hidapi4k.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import io.github.nathanieloliveira.hidapi4k.HidApi
import io.github.nathanieloliveira.hidapi4k.Loader
import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout
import kotlin.experimental.and

class ListHid(): CliktCommand(
    name = "ls"
) {
    override fun help(context: Context): String {
        return """
            List all HID devices.
        """.trimIndent()
    }

    override fun run() {
        val list = HidApi.hidEnumerate(0, 0)
        if (list == null) {
            val error = HidApi.hidError(null)
            println(error)
            return
        }

        var dev = list


        while (dev != null) {
            val out = buildString {
                append(dev.path.padEnd(16))
                append(" ")
                append(dev.vendorId.toHexString())
                append(":")
                append(dev.productId.toHexString())
                append(" ")
                append("interfaceNumber: ")
                append(dev.interfaceNumber)
                append(" ")
                append("usagePage: ")
                append((dev.usagePage and 0xFF).toUInt())
                append(" ")
                append("usage: ")
                append(dev.usage)
                append(" ")
                append(dev.productString)
            }
            echo(out)

            dev = dev.next
        }

        HidApi.hidFreeEnumeration(list)
    }
}

fun main(args: Array<String>) {
    try {
        Loader.load()
        require(HidApi.hidInit() >= 0) {
            val error = HidApi.hidError(null)
            "Could not initiaze hidapi. error: $error"
        }

        ListHid().main(args)
    } finally {
        HidApi.hidExit()
    }
}

fun maina2() {
    Loader.load()
    println("Hello Kotlin!")

    val version = HidApi.hidVersion()
    println(version)
    val versionStr = HidApi.hidVersionStr()
    println(versionStr)

    HidApi.hidInit()
    println("inited!")


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