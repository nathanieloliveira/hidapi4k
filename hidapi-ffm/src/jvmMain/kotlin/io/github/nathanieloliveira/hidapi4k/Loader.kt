package io.github.nathanieloliveira.hidapi4k

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

    private fun unpackIfNeeded(dest: File, resourceName: String, deleteOnExit: Boolean): File {
        val file = File(dest, resourceName)
        if (!file.exists()) {
            if (file.exists()) return file
            val tempFile = File.createTempFile("skiko", "", dest)
            if (deleteOnExit)
                file.deleteOnExit()
            Loader::class.java.getResourceAsStream("/$resourceName")!!.use { input ->
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

        val libraryName = System.getProperty(HIDAPI4K_LIBRARY_PATH)
        if (libraryName != null && libraryName.isNotBlank()) {
            val lib = SymbolLookup.libraryLookup(libraryName, arena)
            loaded.set(true)
            return lib
        }

        val dataDir = File(File(dataPath), HIDAPI_LIB_NAME)
        dataDir.mkdirs()

        val platformName = System.mapLibraryName(HIDAPI_LIB_NAME)
        val library = unpackIfNeeded(dataDir, platformName, false)

        val lib = SymbolLookup.libraryLookup(library.absolutePath, arena)
        loaded.set(true)
        return lib
    }

    fun load(): SymbolLookup {
        return lib
    }

}