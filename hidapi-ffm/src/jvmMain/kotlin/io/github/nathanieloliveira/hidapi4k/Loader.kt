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

    // maybe generate using Gradle
    val hashes = mapOf(
         "hidapi4k/macos/x86_64/libhidapi.dylib" to "1a080921c1e7c9140e358aa87c5d8ae3ecaa143fc4b03af1ab214f0e20422da3",
         "hidapi4k/macos/aarch64/libhidapi.dylib" to "c62cffd82abfaca7bb3b5fe98dbf6f167f3b736638df3f25aed23d35284ff1cf",
         "hidapi4k/windows/aarch64/hidapi.dll" to "df79401844e92e74fb253cb343c39ebc42e9e7e70d75c33c46b7d718ce5f97cc",
         "hidapi4k/linux/x86_64/libhidapi.so" to "e1a4f190524e20923f4c3e126a3c33f1badd7411c592270e6e3978ed91d59c29",
         "hidapi4k/linux/aarch64/libhidapi.so" to "5f0bf65216894e46ecb646c4b530d89590ad4872c8836892ae38c90fd199c2b2",
         "hidapi4k/windows/x86_64/hidapi.dll" to "d4c05ba2138cb5259a7f796464b5eaa63b9f8f67bb5cb003f96989244ee01583",
    )

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

        val libraryPath = System.getenv(HIDAPI4K_LIBRARY_PATH)
        if (libraryPath != null && libraryPath.isNotBlank()) {
            val lib = SymbolLookup.libraryLookup(libraryPath, arena)
            loaded.set(true)
            return lib
        }

        val libraryName = System.mapLibraryName(HIDAPI_LIB_NAME)

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

        val hash = hashes[resourceName] ?: error("no hash for $resourceName")
        val folder = File(dataPath).resolve(hash)
        folder.mkdirs()

        val library = unpackIfNeeded(folder, libraryName, resourceName, false)
        val lib = SymbolLookup.libraryLookup(library.absolutePath, arena)
        loaded.set(true)
        return lib
    }

    fun load(): SymbolLookup {
        return lib
    }

}