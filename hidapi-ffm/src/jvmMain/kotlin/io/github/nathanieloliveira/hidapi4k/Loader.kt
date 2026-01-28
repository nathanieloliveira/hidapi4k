package io.github.nathanieloliveira.hidapi4k

import com.sun.jna.Platform
import java.io.File
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

object Loader {

    const val HIDAPI_LIB_NAME = "hidapi"
    const val HIDAPI4K_LIBRARY_PATH = "HIDAPI4K_LIBRARY_PATH"

    private val dataPath: String get() = System.getProperty("hidapi4k.data.path") ?: "${System.getProperty("user.home")}/.hidapi4k/"
    private val arena = Arena.ofAuto()
    internal val linker = Linker.nativeLinker()

    private val loaded = AtomicBoolean(false)
    val lib by lazy {
        loadInternal()
    }

    private fun unpackIfNeeded(dest: File, libname: String, resourceName: String, deleteOnExit: Boolean): File {
        val file = File(dest, libname)
        if (!file.exists()) {
            if (file.exists()) return file
            val tempFile = File.createTempFile("hidapi4k", "", dest)
            if (deleteOnExit)
                file.deleteOnExit()
            val resourceStream = Loader::class.java.getResourceAsStream("/$resourceName") ?: error("Could not unpack: $resourceName")
            resourceStream.use { input ->
                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }
        return file
    }

    private fun loadInternal(): SymbolLookup {
        if (loaded.get()) {
            return lib
        }

        val libraryPath = System.getProperty(HIDAPI4K_LIBRARY_PATH)
        if (libraryPath != null && libraryPath.isNotBlank()) {
            val lib = SymbolLookup.libraryLookup(libraryPath, arena)
            loaded.set(true)
            return lib
        }

        val libraryName = System.mapLibraryName(HIDAPI_LIB_NAME)

        val folder = File(dataPath)
        folder.mkdirs()

        val osName = when {
            Platform.isWindows() -> "windows"
            Platform.isMac() -> "macos"
            Platform.isLinux() -> "linux"
            else -> error("Unsupported platform: ${Platform.getOSType()}")
        }
        val arch = if (Platform.isARM()) {
            "aarch64"
        } else {
            "x86_64"
        }
        val resourceName = "hidapi4k/$osName/$arch/$libraryName"
        val library = unpackIfNeeded(folder, libraryName, resourceName, false)
        val lib = SymbolLookup.libraryLookup(library.absolutePath, arena)
        loaded.set(true)
        return lib
    }

    fun load(): SymbolLookup {
        return lib
    }

}