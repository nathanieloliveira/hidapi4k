package com.github.nathanieloliveira

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "hidapi4k",
    ) {
        App()
    }
}