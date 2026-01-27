package com.github.nathanieloliveira.hidapi4k

fun main() {
    Loader.load()
    println("Hello Kotlin!")

    HidApiRaw.hidInit()
    println("inited!")

//    val list = HidApiRaw.hidEnumerate(0x1212, 0x1414)
    val list = HidApiRaw.hidEnumerate(0, 0)
    if (list == null) {
        val error = HidApiRaw.hidError(0L)
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
        HidApiRaw.hidClose(open)
    }
}