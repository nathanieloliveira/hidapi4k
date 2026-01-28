package io.github.nathanieloliveira.hidapi4k.cli

import io.github.nathanieloliveira.hidapi4k.HidApi
import io.github.nathanieloliveira.hidapi4k.Loader

fun main() {
    Loader.load()

    val version = HidApi.hidVersionStr()
    println("hidapi4k: $version")
}